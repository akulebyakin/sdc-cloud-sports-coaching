package com.kulebiakin.common.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewMessageTest {

    @Test
    void builder_createsObjectWithAllFields() {
        LocalDateTime timestamp = LocalDateTime.now();

        ReviewMessage message = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Great session!")
            .timestamp(timestamp)
            .coachId(2L)
            .build();

        assertThat(message.getSessionId()).isEqualTo(1L);
        assertThat(message.getRating()).isEqualTo(BigDecimal.valueOf(8.5));
        assertThat(message.getComment()).isEqualTo("Great session!");
        assertThat(message.getTimestamp()).isEqualTo(timestamp);
        assertThat(message.getCoachId()).isEqualTo(2L);
    }

    @Test
    void noArgsConstructor_createsEmptyObject() {
        ReviewMessage message = new ReviewMessage();

        assertThat(message.getSessionId()).isNull();
        assertThat(message.getRating()).isNull();
        assertThat(message.getComment()).isNull();
        assertThat(message.getTimestamp()).isNull();
        assertThat(message.getCoachId()).isNull();
    }

    @Test
    void allArgsConstructor_createsObjectWithAllFields() {
        LocalDateTime timestamp = LocalDateTime.now();

        ReviewMessage message = new ReviewMessage(1L, BigDecimal.valueOf(9.0), "Excellent!", timestamp, 3L);

        assertThat(message.getSessionId()).isEqualTo(1L);
        assertThat(message.getRating()).isEqualTo(BigDecimal.valueOf(9.0));
        assertThat(message.getComment()).isEqualTo("Excellent!");
        assertThat(message.getTimestamp()).isEqualTo(timestamp);
        assertThat(message.getCoachId()).isEqualTo(3L);
    }

    @Test
    void setters_updateFields() {
        ReviewMessage message = new ReviewMessage();
        LocalDateTime timestamp = LocalDateTime.now();

        message.setSessionId(5L);
        message.setRating(BigDecimal.valueOf(7.5));
        message.setComment("Good session");
        message.setTimestamp(timestamp);
        message.setCoachId(10L);

        assertThat(message.getSessionId()).isEqualTo(5L);
        assertThat(message.getRating()).isEqualTo(BigDecimal.valueOf(7.5));
        assertThat(message.getComment()).isEqualTo("Good session");
        assertThat(message.getTimestamp()).isEqualTo(timestamp);
        assertThat(message.getCoachId()).isEqualTo(10L);
    }

    @Test
    void equals_returnsTrueForSameValues() {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 10, 0);

        ReviewMessage message1 = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.0))
            .comment("Test")
            .timestamp(timestamp)
            .coachId(1L)
            .build();

        ReviewMessage message2 = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.0))
            .comment("Test")
            .timestamp(timestamp)
            .coachId(1L)
            .build();

        assertThat(message1).hasSameHashCodeAs(message2).isEqualTo(message2);
    }
}
