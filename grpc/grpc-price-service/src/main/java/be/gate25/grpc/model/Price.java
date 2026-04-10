package be.gate25.grpc.model;

import java.time.Instant;

/**
 * Internal domain model representing the price of a financial instrument.
 * Deliberately decoupled from the gRPC contract (proto-generated classes).
 */
public class Price {

    private final String symbol;
    private final double value;
    private final String currency;
    private final Instant timestamp;

    public Price(String symbol, double value, String currency, Instant timestamp) {
        this.symbol = symbol;
        this.value = value;
        this.currency = currency;
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
