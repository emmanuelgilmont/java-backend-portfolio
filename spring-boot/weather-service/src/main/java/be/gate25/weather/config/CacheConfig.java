package be.gate25.weather.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(WeatherProperties.class)
public class CacheConfig {
    public static final String CACHE_GEOCODING = "geocoding";
    public static final String CACHE_FORECAST  = "forecast";

    @Bean CacheManager cacheManager(WeatherProperties props) {
        var geo = new CaffeineCache(
            CACHE_GEOCODING,
            Caffeine.newBuilder().expireAfterWrite(props.cache().geocodingTtl()).maximumSize(10_000).build()
        );
        var forecast = new CaffeineCache(
            CACHE_FORECAST,
            Caffeine.newBuilder().expireAfterWrite(props.cache().forecastTtl()).maximumSize(10_000).build()
        );

        var mgr = new SimpleCacheManager();
        mgr.setCaches(List.of(geo, forecast));
        return mgr;
    }
}