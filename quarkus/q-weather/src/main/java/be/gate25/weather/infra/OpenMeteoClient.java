package be.gate25.weather.infra;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import be.gate25.weather.config.WeatherProperties;
import be.gate25.weather.domain.WMOWeatherCode;
import be.gate25.weather.domain.WeatherResponse;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OpenMeteoClient {
    private static final Logger  log      = LoggerFactory.getLogger(OpenMeteoClient.class);
    private static final Pattern POSTCODE = Pattern.compile("\\b(\\d{4})\\b");

    private final WeatherProperties props;
    private final GeocodingClient   geoClient;
    private final ForecastClient    forecastClient;

    public OpenMeteoClient(WeatherProperties props, @RestClient GeocodingClient geoClient,
        @RestClient ForecastClient forecastClient) {
        this.props = props;
        this.geoClient = geoClient;
        this.forecastClient = forecastClient;
    }

    public WeatherResponse fetchWeather(String place, int days) {
        var resolved = resolvePlaceWithFallback(place);
        return fetchForecast(
            resolved.name(),
                resolved.postcode(),
                resolved.countryCode(),
                resolved.latitude(),
                resolved.longitude(),
                days);
    }

    private ResolvedPlace resolvePlaceWithFallback(String place) {
        var first = geocode(place);
        if (first.isPresent())
            return first.get();

        // fallback uniquement si on est sur le defaultQuery (sinon surprise pour l’utilisateur)
        if (place.equalsIgnoreCase(props.defaultQuery())) {
            for (String fb : props.fallback()) {
                var r = geocode(fb);
                if (r.isPresent())
                    return r.get();
            }
        }
        throw new NoSuchElementException("Place introuvable: " + place);
    }

    // @CacheResult(cacheName = "geocoding")
    public Optional<ResolvedPlace> geocode(String query) {
        JsonNode root = geoClient.search(query, 10, props.language(), "json", props.countryCode());
        log.info("geocode response for '{}': {}", query, root.textValue());

        if (root == null || root.get("results") == null || !root.get("results").isArray())
            return Optional.empty();

        List<JsonNode> results = new ArrayList<>();
        root.get("results").forEach(results::add);

        // country filter
        results.removeIf(n -> !props.countryCode().equalsIgnoreCase(n.path("country_code").asText("")));

        if (results.isEmpty())
            return Optional.empty();

        String postcodeWanted = extractPostcode(query).orElse(null);

        // property: result contains postal code
        if (postcodeWanted != null) {
            for (JsonNode n : results) {
                if (hasPostcode(n, postcodeWanted))
                    return Optional.of(toResolved(n, postcodeWanted));
            }
        }

        // otherwise the first result (Open-Meteo usually sorts by relevance)
        JsonNode best = results.get(0);
        return Optional.of(toResolved(best, postcodeWanted));
    }

    // @Cacheable(cacheNames = CacheConfig.CACHE_FORECAST, key = "#lat + '|' + #lon + '|' + #days")
    @CacheResult(cacheName = "forecast")
    public WeatherResponse fetchForecast(String name, String postcode, String countryCode, double lat, double lon, int days) {
        String queryCurrent = String
            .join(",", "temperature_2m", "precipitation", "wind_speed_10m", "wind_direction_10m", "weather_code");
        String queryDaily   = String
            .join(
                ",",
                    "temperature_2m_max",
                    "temperature_2m_min",
                    "precipitation_sum",
                    "precipitation_probability_max",
                    "weather_code",
                    "uv_index_max",
                    "wind_speed_10m_max",
                    "wind_direction_10m_dominant");

        JsonNode root = forecastClient.forecast(lat, lon, props.timezone(), days, queryCurrent, queryDaily);

        if (root == null)
            throw new IllegalStateException("Open-Meteo empty answer");

        String tz = root.path("timezone").asText("Europe/Brussels");

        // just because we need to extract the uv index
        JsonNode daily = root.path("daily");

        // current
        JsonNode cur     = root.path("current");
        var      current = new WeatherResponse.Current(cur.path("temperature_2m").asDouble(),
            cur.path("wind_speed_10m").asDouble(), cur.path("wind_direction_10m").asInt(), cur.path("precipitation").asDouble(),
            cur.path("weather_code").asInt(), WMOWeatherCode.getMeaningFromCode(cur.path("weather_code").asInt()));

        // daily arrays
        var times     = daily.path("time");
        var tMax      = daily.path("temperature_2m_max");
        var tMin      = daily.path("temperature_2m_min");
        var pSum      = daily.path("precipitation_sum");
        var pProb     = daily.path("precipitation_probability_max");
        var wMax      = daily.path("wind_speed_10m_max");
        var wCode     = daily.path("weather_code");
        var uvIndex   = daily.path("uv_index_max");
        var windSpeed = daily.path("wind_speed_10m_max");
        var windDir   = daily.path("wind_direction_10m_dominant");

        List<WeatherResponse.Daily> daysOut = new ArrayList<>();
        int                         n       = times.isArray() ? times.size() : 0;
        for (int i = 0; i < n; i++) {
            daysOut
                .add(
                    new WeatherResponse.Daily(times.get(i).asText(), tMax.get(i).asDouble(), tMin.get(i).asDouble(),
                        pSum.get(i).asDouble(), pProb.get(i).asInt(), wMax.get(i).asDouble(), wCode.get(i).asInt(),
                        WMOWeatherCode.getMeaningFromCode(wCode.get(i).asInt()), uvIndex.get(i).asDouble(),
                        windSpeed.get(i).asDouble(), windDir.get(i).asInt()));
        }

        var place = new WeatherResponse.Place(name, postcode, countryCode, tz);
        return new WeatherResponse(place, current, daysOut);
    }

    private Optional<String> extractPostcode(String s) {
        Matcher m = POSTCODE.matcher(s);
        return m.find() ? Optional.of(m.group(1)) : Optional.empty();
    }

    private boolean hasPostcode(JsonNode node, String wanted) {
        JsonNode postcodes = node.get("postcodes");
        if (postcodes == null || !postcodes.isArray())
            return false;
        for (JsonNode pc : postcodes)
            if (wanted.equals(pc.asText()))
                return true;
        return false;
    }

    private ResolvedPlace toResolved(JsonNode n, String postcode) {
        return new ResolvedPlace(n.path("name").asText(), postcode, n.path("country_code").asText(),
            n.path("latitude").asDouble(), n.path("longitude").asDouble());
    }

    public record ResolvedPlace(String name, String postcode, String countryCode, double latitude, double longitude) {
    }

}