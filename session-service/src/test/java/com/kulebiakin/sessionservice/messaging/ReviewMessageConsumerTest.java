package com.kulebiakin.sessionservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kulebiakin.common.dto.ReviewMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ReviewMessage serialization used by ReviewMessageConsumer.
 * The actual ServiceBusProcessorClient is tested in integration tests.
 */
class ReviewMessageConsumerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void reviewMessage_serialization_roundTrip() throws Exception {
        ReviewMessage original = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Great session!")
            .timestamp(LocalDateTime.of(2024, 1, 15, 10, 30))
            .coachId(2L)
            .build();

        String json = objectMapper.writeValueAsString(original);
        ReviewMessage deserialized = objectMapper.readValue(json, ReviewMessage.class);

        assertThat(deserialized.getSessionId()).isEqualTo(1L);
        assertThat(deserialized.getRating()).isEqualByComparingTo(BigDecimal.valueOf(8.5));
        assertThat(deserialized.getComment()).isEqualTo("Great session!");
        assertThat(deserialized.getCoachId()).isEqualTo(2L);
    }

    @Test
    void reviewMessage_jsonSerialization_containsExpectedFields() throws Exception {
        ReviewMessage original = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Test comment")
            .timestamp(LocalDateTime.now())
            .coachId(1L)
            .build();

        String json = objectMapper.writeValueAsString(original);

        assertThat(json).contains("\"sessionId\"");
        assertThat(json).contains("\"rating\"");
        assertThat(json).contains("\"comment\"");
        assertThat(json).contains("\"coachId\"");

        ReviewMessage deserialized = objectMapper.readValue(json, ReviewMessage.class);

        assertThat(deserialized.getSessionId()).isEqualTo(original.getSessionId());
        assertThat(deserialized.getRating()).isEqualByComparingTo(original.getRating());
        assertThat(deserialized.getComment()).isEqualTo(original.getComment());
    }

    @Test
    void reviewMessage_nullComment_serializesCorrectly() throws Exception {
        ReviewMessage original = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(7.0))
            .comment(null)
            .timestamp(LocalDateTime.now())
            .coachId(1L)
            .build();

        String json = objectMapper.writeValueAsString(original);
        ReviewMessage deserialized = objectMapper.readValue(json, ReviewMessage.class);

        assertThat(deserialized.getComment()).isNull();
        assertThat(deserialized.getSessionId()).isEqualTo(1L);
        assertThat(deserialized.getRating()).isEqualByComparingTo(BigDecimal.valueOf(7.0));
    }
}
