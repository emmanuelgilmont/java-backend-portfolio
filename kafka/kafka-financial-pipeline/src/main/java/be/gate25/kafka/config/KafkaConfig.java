package be.gate25.kafka.config;

import be.gate25.kafka.model.TradeEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // -------------------------------------------------------------------------
    // Topics
    // -------------------------------------------------------------------------

    @Bean
    public NewTopic tradeEventsTopic() {
        return TopicBuilder.name(KafkaTopics.TRADE_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic tradeEventsProcessedTopic() {
        return TopicBuilder.name(KafkaTopics.TRADE_EVENTS_PROCESSED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic tradeEventsDlqTopic() {
        return TopicBuilder.name(KafkaTopics.TRADE_EVENTS_DLQ)
                .partitions(1)
                .replicas(1)
                .build();
    }

    // -------------------------------------------------------------------------
    // Producer
    // -------------------------------------------------------------------------

    @Bean
    public ProducerFactory<String, TradeEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");           // strongest durability guarantee
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, TradeEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // -------------------------------------------------------------------------
    // Consumer
    // -------------------------------------------------------------------------

    @Bean
    public ConsumerFactory<String, TradeEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "trade-pipeline");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "be.gate25.kafka.model");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TradeEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TradeEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TradeEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
