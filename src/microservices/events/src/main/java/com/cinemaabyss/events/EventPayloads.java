package com.cinemaabyss.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class EventPayloads {
    private EventPayloads() { }

    public record MovieEvent(
            @NotNull @JsonProperty("movie_id") Integer movieId,
            @NotBlank String title,
            @NotBlank String action,
            @JsonProperty("user_id") Integer userId,
            Double rating,
            List<String> genres,
            String description) { }

    public record UserEvent(
            @NotNull @JsonProperty("user_id") Integer userId,
            @NotBlank String action,
            @NotNull OffsetDateTime timestamp,
            String username,
            String email) { }

    public record PaymentEvent(
            @NotNull @JsonProperty("payment_id") Integer paymentId,
            @NotNull @JsonProperty("user_id") Integer userId,
            @NotNull BigDecimal amount,
            @NotBlank String status,
            @NotNull OffsetDateTime timestamp,
            @JsonProperty("method_type") String methodType) { }
}
