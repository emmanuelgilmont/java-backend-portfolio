package be.gate25.kafka.producer;

import be.gate25.kafka.config.KafkaTopics;
import be.gate25.kafka.model.TradeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TradeEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TradeEventProducer.class);

    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;

    public TradeEventProducer(KafkaTemplate<String, TradeEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends a TradeEvent to the {@code trade-events} topic.
     * The eventId is used as the Kafka message key — guarantees ordering per trade.
     */
    public void send(TradeEvent event) {
        log.debug("Sending TradeEvent: {}", event);

        CompletableFuture<SendResult<String, TradeEvent>> future =
                kafkaTemplate.send(KafkaTopics.TRADE_EVENTS, event.getEventId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send TradeEvent [eventId={}]: {}", event.getEventId(), ex.getMessage());
            } else {
                log.debug("TradeEvent sent [eventId={}, partition={}, offset={}]",
                        event.getEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
