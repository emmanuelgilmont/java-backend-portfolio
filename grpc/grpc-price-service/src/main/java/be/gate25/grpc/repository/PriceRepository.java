package be.gate25.grpc.repository;

import be.gate25.grpc.model.Price;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;

/**
 * Stub repository with hardcoded prices.
 * Replace with a real data source (DB, cache, market feed) in production.
 */
@Repository
public class PriceRepository {

    private static final Map<String, Price> PRICES = Map.of(
        "BEL20:UCB",  new Price("BEL20:UCB",  142.30, "EUR", Instant.now()),
        "BEL20:KBC",  new Price("BEL20:KBC",   68.50, "EUR", Instant.now()),
        "BEL20:GLPG", new Price("BEL20:GLPG", 112.75, "EUR", Instant.now())
    );

    public Price findBySymbol(String symbol) {
        return PRICES.get(symbol);
    }
}