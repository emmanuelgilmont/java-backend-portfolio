package be.gate25.fxrate.provider;

import be.gate25.fxrate.domain.FxRate;

/**
 * Contract for fetching a live FX rate from an external source.
 * In production this would call e.g. ECB / Bloomberg / Refinitiv.
 * The stub simulates network latency so tests can prove the cache avoids it.
 */
public interface ExternalRateProvider {

    /**
     * Fetch the current rate for the given currency pair.
     *
     * @param pair  e.g. "EUR/USD"
     * @return fresh {@link FxRate}
     * @throws UnsupportedPairException if the pair is not supported
     */
    FxRate fetch(String pair);
}
