package be.gate25.fxrate.provider;

import be.gate25.fxrate.domain.FxRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stub implementation that simulates a slow external FX provider.
 * Tracks the number of actual (non-cached) calls — useful in tests to assert
 * that the cache prevents redundant remote calls.
 */
@Component
public class StubExternalRateProvider implements ExternalRateProvider {

    private static final Logger log = LoggerFactory.getLogger(StubExternalRateProvider.class);

    /** Simulated base rates (mid-price, illustrative only). */
    private static final Map<String, BigDecimal> BASE_RATES = Map.of(
            "EUR/USD", new BigDecimal("1.0850"),
            "EUR/GBP", new BigDecimal("0.8560"),
            "EUR/JPY", new BigDecimal("163.50"),
            "USD/JPY", new BigDecimal("150.70"),
            "GBP/USD", new BigDecimal("1.2680")
    );

    /** Simulated network latency (ms). Kept short so tests are not slow. */
    private static final long SIMULATED_LATENCY_MS = 80;

    private final AtomicInteger callCount = new AtomicInteger(0);

    @Override
    public FxRate fetch(String pair) {
        String normalised = pair.toUpperCase();
        BigDecimal base = BASE_RATES.get(normalised);

        // Count the calls before applying any logic — including unknown pairs
        int count = callCount.incrementAndGet();
        log.info("External provider call #{} for pair {}", count, normalised);

        if (base == null) {
            throw new UnsupportedPairException(pair);
        }

        simulateLatency();

        // Add tiny jitter to simulate live feed (±0.0005)
        BigDecimal jitter = BigDecimal.valueOf(Math.random() * 0.001 - 0.0005)
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal rate = base.add(jitter).setScale(4, RoundingMode.HALF_UP);

        return FxRate.of(normalised, rate);
    }

    /** Returns the total number of actual provider calls since startup / reset. */
    public int getCallCount() {
        return callCount.get();
    }

    /** Resets the call counter — useful between test cases. */
    public void resetCallCount() {
        callCount.set(0);
    }

    private void simulateLatency() {
        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}