package be.gate25.fxrate.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable FX rate snapshot for a currency pair (e.g. EUR/USD).
 * Serializable so Redis can store it as bytes (Java serialization) or JSON.
 */
public record FxRate(
        String pair,
        BigDecimal rate,
        String baseCurrency,
        String quoteCurrency,
        Instant fetchedAt
) implements Serializable {

    /** Convenience factory — splits "EUR/USD" into base / quote. */
    public static FxRate of(String pair, BigDecimal rate) {
        String[] parts = pair.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid pair format, expected BASE/QUOTE: " + pair);
        }
        return new FxRate(pair, rate, parts[0], parts[1], Instant.now());
    }
}
