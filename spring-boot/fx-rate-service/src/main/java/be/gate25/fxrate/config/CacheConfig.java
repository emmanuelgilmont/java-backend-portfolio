package be.gate25.fxrate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration.
 *
 * <p>Two named caches with distinct TTLs:
 * <ul>
 *   <li>{@code fx-rates}      — live rates, short TTL (default 60 s)</li>
 *   <li>{@code fx-rates-meta} — static pair metadata, longer TTL (default 300 s)</li>
 * </ul>
 *
 * <p>Values are serialized as JSON (Jackson) so they are human-readable in Redis CLI
 * and survive application restarts without class-path coupling.
 */
@Configuration
@org.springframework.context.annotation.Profile("!test")
public class CacheConfig {

    /** Cache names — single source of truth, also used in @Cacheable annotations. */
    public static final String CACHE_FX_RATES      = "fx-rates";
    public static final String CACHE_FX_RATES_META = "fx-rates-meta";

    private final CacheTtlProperties ttlProperties;

    public CacheConfig(CacheTtlProperties ttlProperties) {
        this.ttlProperties = ttlProperties;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = defaultCacheConfig();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CACHE_FX_RATES,
                defaultConfig.entryTtl(Duration.ofSeconds(ttlProperties.getFxRatesTtl())));
        cacheConfigs.put(CACHE_FX_RATES_META,
                defaultConfig.entryTtl(Duration.ofSeconds(ttlProperties.getFxRatesMetaTtl())));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    private RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                // Keys: plain strings — readable in redis-cli
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // Values: JSON — survives restarts, inspectable
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
    }

    // ---------------------------------------------------------------------------
    // TTL properties — bound from application.yml fx-rate.cache.ttl-seconds.*
    // ---------------------------------------------------------------------------

    @ConfigurationProperties(prefix = "fx-rate.cache.ttl-seconds")
    @org.springframework.stereotype.Component
    public static class CacheTtlProperties {

        private long fxRates = 60;
        private long fxRatesMeta = 300;

        public long getFxRatesTtl()     { return fxRates; }
        public long getFxRatesMetaTtl() { return fxRatesMeta; }

        public void setFxRates(long fxRates)         { this.fxRates = fxRates; }
        public void setFxRatesMeta(long fxRatesMeta) { this.fxRatesMeta = fxRatesMeta; }
    }
}