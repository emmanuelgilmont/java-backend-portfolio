package be.gate25.weather.infra;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import be.gate25.weather.config.CacheConfig;
import be.gate25.weather.config.WeatherProperties;
import be.gate25.weather.domain.WMOWeatherCode;
import be.gate25.weather.domain.WeatherResponse;
import tools.jackson.databind.JsonNode;

@Component
public class OpenMeteoClient {
    private static final Pattern    POSTCODE = Pattern.compile("\\b(\\d{4})\\b");

    private final WeatherProperties props;
    private final RestClient        geoClient;
    private final RestClient        forecastClient;

    public OpenMeteoClient(WeatherProperties props, RestClient.Builder builder) {
        this.props = props;
        this.geoClient = builder.baseUrl(props.openMeteo().geocodingBaseUrl()).build();
        this.forecastClient = builder.baseUrl(props.openMeteo().forecastBaseUrl()).build();
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

    @Cacheable(cacheNames = CacheConfig.CACHE_GEOCODING, key = "#query")
    public Optional<ResolvedPlace> geocode(String query) {
        JsonNode root = geoClient
            .get()
                .uri(
                    uri -> uri
                        .path("/search")
                            .queryParam("name", query)
                            .queryParam("count", 10)
                            .queryParam("language", props.language())
                            .queryParam("format", "json")
                            .queryParam("countryCode", props.countryCode())
                            .build())
                .retrieve()
                .body(JsonNode.class);

        if (root == null || root.get("results") == null || !root.get("results").isArray())
            return Optional.empty();

        List<JsonNode> results = new ArrayList<>();
        root.get("results").forEach(results::add);

        // filtre pays
        results.removeIf(n -> !props.countryCode().equalsIgnoreCase(n.path("country_code").asText("")));

        if (results.isEmpty())
            return Optional.empty();

        String postcodeWanted = extractPostcode(query).orElse(null);

        // priorité: résultat contenant le code postal
        if (postcodeWanted != null) {
            for (JsonNode n : results) {
                if (hasPostcode(n, postcodeWanted))
                    return Optional.of(toResolved(n, postcodeWanted));
            }
        }

        // sinon premier résultat (Open-Meteo ordonne généralement par pertinence)
        JsonNode best = results.get(0);
        return Optional.of(toResolved(best, postcodeWanted));
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_FORECAST, key = "#lat + '|' + #lon + '|' + #days")
    public WeatherResponse fetchForecast(String name, String postcode, String countryCode, double lat, double lon, int days) {
        JsonNode root = forecastClient
            .get()
                .uri(
                    uri -> uri
                        .path("/forecast")
                            .queryParam("latitude", lat)
                            .queryParam("longitude", lon)
                            .queryParam("timezone", props.timezone())
                            .queryParam("forecast_days", days)
                            .queryParam(
                                "current",
                                    String
                                        .join(
                                            ",",
                                                "temperature_2m",
                                                "precipitation",
                                                "wind_speed_10m",
                                                "wind_direction_10m",
                                                "weather_code"))
                            .queryParam(
                                "daily",
                                    String
                                        .join(
                                            ",",
                                                "temperature_2m_max",
                                                "temperature_2m_min",
                                                "precipitation_sum",
                                                "precipitation_probability_max",
                                                "weather_code",
                                                "uv_index_max",
                                                "wind_speed_10m_max",
                                                "wind_direction_10m_dominant"))
                            .build())
                .retrieve()
                .body(JsonNode.class);

        if (root == null)
            throw new IllegalStateException("Réponse Open-Meteo vide");

        String tz = root.path("timezone").asText("Europe/Brussels");

        // just because we need to extract the uv index
        JsonNode daily = root.path("daily");

        // current
        JsonNode cur = root.path("current");
        var current = new WeatherResponse.Current(cur.path("temperature_2m").asDouble(), cur.path("wind_speed_10m").asDouble(),
            cur.path("wind_direction_10m").asInt(), cur.path("precipitation").asDouble(), cur.path("weather_code").asInt(),
            WMOWeatherCode.getMeaningFromCode(cur.path("weather_code").asInt()));

        // daily arrays
        var times = daily.path("time");
        var tMax = daily.path("temperature_2m_max");
        var tMin = daily.path("temperature_2m_min");
        var pSum = daily.path("precipitation_sum");
        var pProb = daily.path("precipitation_probability_max");
        var wMax = daily.path("wind_speed_10m_max");
        var wCode = daily.path("weather_code");
        var uvIndex = daily.path("uv_index_max");
        var windSpeed = daily.path("wind_speed_10m_max");
        var windDir = daily.path("wind_direction_10m_dominant");

        List<WeatherResponse.Daily> daysOut = new ArrayList<>();
        int n = times.isArray() ? times.size() : 0;
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