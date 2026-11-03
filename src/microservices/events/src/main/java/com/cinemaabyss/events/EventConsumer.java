package com.cinemaabyss.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {
    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    @KafkaListener(topics = "${events.topics.movie}")
    public void consumeMovie(String message) { log.info("Consumed movie event: {}", message); }

    @KafkaListener(topics = "${events.topics.user}")
    public void consumeUser(String message) { log.info("Consumed user event: {}", message); }

    @KafkaListener(topics = "${events.topics.payment}")
    public void consumePayment(String message) { log.info("Consumed payment event: {}", message); }
}
