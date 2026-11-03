package com.cinemaabyss.events;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) { this.eventService = eventService; }

    @GetMapping("/health")
    public Map<String, Boolean> health() { return Map.of("status", true); }

    @PostMapping("/movie")
    public ResponseEntity<EventService.EventResponse> movie(@Valid @RequestBody EventPayloads.MovieEvent event) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.movie(event));
    }

    @PostMapping("/user")
    public ResponseEntity<EventService.EventResponse> user(@Valid @RequestBody EventPayloads.UserEvent event) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.user(event));
    }

    @PostMapping("/payment")
    public ResponseEntity<EventService.EventResponse> payment(@Valid @RequestBody EventPayloads.PaymentEvent event) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.payment(event));
    }
}
