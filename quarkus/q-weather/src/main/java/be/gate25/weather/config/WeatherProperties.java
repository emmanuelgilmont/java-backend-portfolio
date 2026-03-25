package be.gate25.weather.config;

import java.time.Duration;
import java.util.List;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "weather")
public interface WeatherProperties {
    String defaultQuery();

    List<String> fallback();

    String countryCode();

    String language();

    int forecastDays();

    String timezone();

    Cache cache();

    interface Cache {
        Duration geocodingTtl();

        Duration forecastTtl();
    }

}