package be.gate25.fxrate.service;

import be.gate25.fxrate.config.CacheConfig;
import be.gate25.fxrate.domain.FxRate;
import be.gate25.fxrate.provider.ExternalRateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FX rate service.
 *
 * <p>Cache behaviour:
 * <ul>
 *   <li>{@link #getRate(String)} — cache-aside: returns cached value if present,
 *       otherwise delegates to {@link ExternalRateProvider} and caches the result.</li>
 *   <li>{@link #evictRate(String)} — removes a single pair from the cache.</li>
 *   <li>{@link #evictAll()} — clears the entire fx-rates cache (e.g. EOD reset).</li>
 * </ul>
 */
@Service
public class RateService {

    private static final Logger log = LoggerFactory.getLogger(RateService.class);

    /** Supported pairs — in production this would be driven by config or a registry. */
    public static final List<String> SUPPORTED_PAIRS = List.of(
            "EUR/USD", "EUR/GBP", "EUR/JPY", "USD/JPY", "GBP/USD"
    );

    private final ExternalRateProvider provider;

    public RateService(ExternalRateProvider provider) {
        this.provider = provider;
    }

    /**
     * Returns the FX rate for the given pair.
     * On cache miss, fetches from the external provider and populates the cache.
     *
     * @param pair  currency pair in BASE/QUOTE format, e.g. "EUR/USD"
     * @return cached or freshly fetched {@link FxRate}
     */
    @Cacheable(value = CacheConfig.CACHE_FX_RATES, key = "#pair.toUpperCase()")
    public FxRate getRate(String pair) {
        log.info("Cache MISS for pair {} — fetching from external provider", pair);
        return provider.fetch(pair);
    }

    /**
     * Evicts the cached rate for a single pair.
     * Next call to {@link #getRate(String)} will trigger a fresh fetch.
     */
    @CacheEvict(value = CacheConfig.CACHE_FX_RATES, key = "#pair.toUpperCase()")
    public void evictRate(String pair) {
        log.info("Cache evicted for pair {}", pair);
    }

    /**
     * Clears all entries from the fx-rates cache.
     * Useful for EOD processing or forced refresh.
     */
    @CacheEvict(value = CacheConfig.CACHE_FX_RATES, allEntries = true)
    public void evictAll() {
        log.info("fx-rates cache fully cleared");
    }

    public List<String> getSupportedPairs() {
        return SUPPORTED_PAIRS;
    }
}
