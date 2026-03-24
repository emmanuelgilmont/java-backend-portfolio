package be.gate25.weather.service;

import org.springframework.stereotype.Service;

import be.gate25.weather.config.WeatherProperties;
import be.gate25.weather.domain.WeatherResponse;
import be.gate25.weather.domain.WindDirection;
import be.gate25.weather.infra.OpenMeteoClient;

@Service
public class WeatherService {
    private final WeatherProperties props;
    private final OpenMeteoClient   client;

    public WeatherService(WeatherProperties props, OpenMeteoClient client) {
        this.props = props;
        this.client = client;
    }

    public WeatherResponse getWeather(String place, int days) {
        int d = Math.max(1, Math.min(days, props.forecastDays()));
        String q = (place == null || place.isBlank()) ? props.defaultQuery() : place;
        return client.fetchWeather(q, d);
    }

    public WeatherResponse getDefaultWeather() {
        return client.fetchWeather(props.defaultQuery(), props.forecastDays());
    }

    public String getHumanDaily(String place) {
        WeatherResponse w = getWeather(place, 2);
        WeatherResponse.Current c = w.current();
        WeatherResponse.Daily today = w.daily().get(0);
        String template = "Today's weather in %s, %s%n" + "%s, wind %d km/h from the %s.%n"
            + "Temperature: min %.1f°C - max %.1f°C ; currently %.1f°C.%n"
            + "Rain: %.1f mm expected (%d%% chance). UV index max: %.1f.%n";

        return String
            .format(
                template,
                    w.place().name(),
                    today.date(),
                    today.weatherCodeMeaning(),
                    (int) today.windKmh(),
                    WindDirection.fromDegrees(today.windDirectionDeg()),
                    today.tempMinC(),
                    today.tempMaxC(),
                    c.temperatureC(),
                    today.precipSumMm(),
                    today.precipProbMaxPct(),
                    today.uvIndex());
    }
}