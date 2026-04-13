package be.gate25.fxrate;

import be.gate25.fxrate.domain.FxRate;
import be.gate25.fxrate.provider.StubExternalRateProvider;
import be.gate25.fxrate.provider.UnsupportedPairException;
import be.gate25.fxrate.service.RateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests du comportement cache (hit / miss / eviction).
 *
 * Profil "test" → Caffeine en mémoire, aucune infrastructure requise.
 * Tourne avec un simple "mvn test", sans Docker.
 *
 * Ce que ces tests prouvent :
 *   - Le pattern cache-aside fonctionne (@Cacheable / @CacheEvict)
 *   - La normalisation de clé (casse)
 *   - L'isolation par paire de devises
 *
 * Ce qu'ils ne testent PAS (couvert par RateServiceRedisIT) :
 *   - La sérialisation JSON Redis
 *   - Le TTL server-side
 */
@SpringBootTest
@ActiveProfiles("test")
class RateServiceCacheTest {

    @Autowired RateService rateService;
    @Autowired StubExternalRateProvider provider;

    @BeforeEach
    void setUp() {
        rateService.evictAll();
        provider.resetCallCount();
    }

    @Test
    @DisplayName("Premier appel = cache miss → provider appelé une fois")
    void firstCall_isCacheMiss() {
        FxRate rate = rateService.getRate("EUR/USD");

        assertThat(rate).isNotNull();
        assertThat(rate.pair()).isEqualTo("EUR/USD");
        assertThat(provider.getCallCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deuxième appel = cache hit → provider PAS rappelé")
    void secondCall_isCacheHit() {
        rateService.getRate("EUR/USD"); // miss
        rateService.getRate("EUR/USD"); // hit

        assertThat(provider.getCallCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Paires différentes → chacune déclenche un miss distinct")
    void differentPairs_eachTriggerMiss() {
        rateService.getRate("EUR/USD");
        rateService.getRate("EUR/GBP");
        rateService.getRate("EUR/JPY");

        assertThat(provider.getCallCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Normalisation de clé : EUR/USD et eur/usd → même entrée cache")
    void keyNormalisation_caseInsensitive() {
        rateService.getRate("EUR/USD");
        rateService.getRate("eur/usd"); // même clé

        assertThat(provider.getCallCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Après evictRate(), prochain appel = miss")
    void afterEviction_nextCallIsMiss() {
        rateService.getRate("EUR/USD");    // miss → 1
        rateService.evictRate("EUR/USD");
        rateService.getRate("EUR/USD");    // miss → 2

        assertThat(provider.getCallCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("evictRate() n'affecte pas les autres paires en cache")
    void evictOnePair_doesNotAffectOthers() {
        rateService.getRate("EUR/USD");    // miss → 1
        rateService.getRate("EUR/GBP");    // miss → 2
        rateService.evictRate("EUR/USD");
        rateService.getRate("EUR/USD");    // miss → 3
        rateService.getRate("EUR/GBP");    // hit  → reste 3

        assertThat(provider.getCallCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("evictAll() vide tout le cache")
    void evictAll_clearsAllPairs() {
        rateService.getRate("EUR/USD");    // miss → 1
        rateService.getRate("EUR/GBP");    // miss → 2
        rateService.evictAll();
        rateService.getRate("EUR/USD");    // miss → 3
        rateService.getRate("EUR/GBP");    // miss → 4

        assertThat(provider.getCallCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("Paire inconnue → UnsupportedPairException, pas de pollution cache")
    void unknownPair_throwsAndDoesNotCache() {
        assertThatThrownBy(() -> rateService.getRate("XYZ/ABC"))
                .isInstanceOf(UnsupportedPairException.class);

        assertThat(provider.getCallCount()).isEqualTo(1);
    }
}
