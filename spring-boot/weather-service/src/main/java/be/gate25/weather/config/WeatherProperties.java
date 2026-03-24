package be.gate25.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "weather")
public record WeatherProperties (
    String defaultQuery,
    List<String> fallback,
    String countryCode,
    String language,
    int forecastDays,
    String timezone,
    Cache cache,
    OpenMeteo openMeteo)
{
    public record Cache(Duration geocodingTtl, Duration forecastTtl) {}
    public record OpenMeteo(String geocodingBaseUrl, String forecastBaseUrl) {}
}