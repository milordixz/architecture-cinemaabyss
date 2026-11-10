package com.cinemaabyss.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class EventService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String movieTopic;
    private final String userTopic;
    private final String paymentTopic;

    public EventService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper,
                        @Value("${events.topics.movie}") String movieTopic,
                        @Value("${events.topics.user}") String userTopic,
                        @Value("${events.topics.payment}") String paymentTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.movieTopic = movieTopic;
        this.userTopic = userTopic;
        this.paymentTopic = paymentTopic;
    }

    public EventResponse publish(String type, String topic, Object payload) {
        String id = UUID.randomUUID().toString();
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                    "id", id, "type", type, "timestamp", OffsetDateTime.now().toString(), "payload", payload));
            SendResult<String, String> result = kafkaTemplate.send(topic, id, message).get();
            RecordMetadata metadata = result.getRecordMetadata();
            return new EventResponse("success", metadata.partition(), metadata.offset());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing event", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Cannot publish event to Kafka", e.getCause());
        }
    }

    public EventResponse movie(EventPayloads.MovieEvent event) { return publish("movie", movieTopic, event); }
    public EventResponse user(EventPayloads.UserEvent event) { return publish("user", userTopic, event); }
    public EventResponse payment(EventPayloads.PaymentEvent event) { return publish("payment", paymentTopic, event); }

    public record EventResponse(String status, int partition, long offset) { }
}
