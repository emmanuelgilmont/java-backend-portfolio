package be.gate25.kafka.config;

/**
 * Centralized topic name constants.
 * Single source of truth — never hardcode topic names elsewhere.
 */
public final class KafkaTopics {

    public static final String TRADE_EVENTS           = "trade-events";
    public static final String TRADE_EVENTS_PROCESSED = "trade-events-processed";
    public static final String TRADE_EVENTS_DLQ       = "trade-events-dlq";

    private KafkaTopics() {}
}
