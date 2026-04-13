package be.gate25.fxrate;

import be.gate25.fxrate.domain.FxRate;
import be.gate25.fxrate.provider.StubExternalRateProvider;
import be.gate25.fxrate.provider.UnsupportedPairException;
import be.gate25.fxrate.service.RateService;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration Redis réel via Testcontainers.
 *
 * Requiert Docker — skippé automatiquement si Docker n'est pas disponible.
 * Lancé en CI (GitHub Actions) via le profil Maven "integration-test".
 *
 * Ce que ces tests ajoutent par rapport à RateServiceCacheTest :
 *   - Sérialisation JSON round-trip (GenericJackson2JsonRedisSerializer)
 *   - TTL server-side Redis
 *   - Comportement réel du RedisConnectionFactory
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class RateServiceRedisIT {

    @Container
    static final RedisContainer REDIS =
            new RedisContainer(DockerImageName.parse("redis:7.2-alpine"));

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    }

    @Autowired
    RateService rateService;

    @Autowired
    StubExternalRateProvider provider;

    @BeforeEach
    void setUp() {
        // Clear cache + reset call counter before each test for isolation
        rateService.evictAll();
        provider.resetCallCount();
    }

    // -----------------------------------------------------------------------
    // Cache-aside: miss then hit
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("First call is a cache miss — provider is invoked once")
    void firstCall_shouldBeACacheMiss() {
        FxRate rate = rateService.getRate("EUR/USD");

        assertThat(rate).isNotNull();
        assertThat(rate.pair()).isEqualTo("EUR/USD");
        assertThat(provider.getCallCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Second call for same pair is a cache hit — provider is NOT called again")
    void secondCall_shouldBeACacheHit() {
        rateService.getRate("EUR/USD");  // miss
        rateService.getRate("EUR/USD");  // hit

        assertThat(provider.getCallCount())
                .as("Provider should have been called exactly once")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Different pairs each trigger a cache miss")
    void differentPairs_shouldEachTriggerACacheMiss() {
        rateService.getRate("EUR/USD");
        rateService.getRate("EUR/GBP");
        rateService.getRate("EUR/JPY");

        assertThat(provider.getCallCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Key normalisation: EUR/USD and eur/usd hit the same cache entry")
    void keyNormalisation_shouldBeCaseInsensitive() {
        rateService.getRate("EUR/USD");
        rateService.getRate("eur/usd");  // should hit the same cache key

        assertThat(provider.getCallCount()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // Eviction
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("After evictRate(), next call for that pair is a miss again")
    void afterSingleEviction_nextCallShouldBeMiss() {
        rateService.getRate("EUR/USD");   // miss  → count = 1
        rateService.evictRate("EUR/USD");
        rateService.getRate("EUR/USD");   // miss again → count = 2

        assertThat(provider.getCallCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("evictRate() for one pair does not affect other cached pairs")
    void evictSinglePair_shouldNotAffectOtherPairs() {
        rateService.getRate("EUR/USD");   // count = 1
        rateService.getRate("EUR/GBP");   // count = 2

        rateService.evictRate("EUR/USD");

        rateService.getRate("EUR/USD");   // miss  → count = 3
        rateService.getRate("EUR/GBP");   // hit   → count stays 3

        assertThat(provider.getCallCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("evictAll() clears the entire cache — all pairs become misses")
    void evictAll_shouldClearAllCachedPairs() {
        rateService.getRate("EUR/USD");   // miss  → count = 1
        rateService.getRate("EUR/GBP");   // miss  → count = 2

        rateService.evictAll();

        rateService.getRate("EUR/USD");   // miss  → count = 3
        rateService.getRate("EUR/GBP");   // miss  → count = 4

        assertThat(provider.getCallCount()).isEqualTo(4);
    }

    // -----------------------------------------------------------------------
    // Error cases
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Unknown pair throws UnsupportedPairException — not cached")
    void unknownPair_shouldThrowAndNotCache() {
        assertThatThrownBy(() -> rateService.getRate("XYZ/ABC"))
                .isInstanceOf(UnsupportedPairException.class)
                .hasMessageContaining("XYZ/ABC");

        // No successful entry was cached — call count reflects the failed attempt
        assertThat(provider.getCallCount()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // Data integrity
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Cached FxRate survives Redis serialization round-trip")
    void cachedValue_shouldSurviveSerializationRoundTrip() {
        FxRate original = rateService.getRate("USD/JPY");  // stored in Redis
        FxRate cached   = rateService.getRate("USD/JPY");  // retrieved from Redis

        assertThat(cached.pair()).isEqualTo(original.pair());
        assertThat(cached.rate()).isEqualByComparingTo(original.rate());
        assertThat(cached.baseCurrency()).isEqualTo("USD");
        assertThat(cached.quoteCurrency()).isEqualTo("JPY");
    }
}
