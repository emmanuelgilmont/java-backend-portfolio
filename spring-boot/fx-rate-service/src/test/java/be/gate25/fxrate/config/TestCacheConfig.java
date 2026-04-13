package be.gate25.fxrate.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

/**
 * Configuration de cache pour les tests locaux (profil "test").
 *
 * Remplace {@link CacheConfig} (qui nécessite un RedisConnectionFactory)
 * par un CaffeineCacheManager en mémoire — même abstraction Spring Cache,
 * zéro infrastructure requise.
 */
@Configuration
@Profile("test")
public class TestCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                CacheConfig.CACHE_FX_RATES,
                CacheConfig.CACHE_FX_RATES_META
        );
        manager.setCaffeine(
                Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS)
        );
        return manager;
    }
}
