package com.kulebiakin.sessionservice.messaging;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.sessionservice.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for ReviewMessageConsumer.
 * Note: Full integration testing of Azure Storage Queue processing
 * should be done with integration tests using Testcontainers or Azure emulator.
 */
@ExtendWith(MockitoExtension.class)
class ReviewMessageConsumerTest {

    @Mock
    private QueueClient reviewEventsQueueClient;

    @Mock
    private SessionService sessionService;

    private ObjectMapper objectMapper;
    private ReviewMessageConsumer reviewMessageConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        reviewMessageConsumer = new ReviewMessageConsumer(reviewEventsQueueClient, sessionService, objectMapper);
    }

    @Test
    void pollMessages_emptyQueue_doesNotProcessAnything() {
        List<QueueMessageItem> items = Collections.emptyList();
        doAnswer(invocation -> (Iterable<QueueMessageItem>) items::iterator)
            .when(reviewEventsQueueClient).receiveMessages(anyInt(), any(), any(), any());

        reviewMessageConsumer.pollMessages();

        verify(sessionService, never()).processReview(any(), any(), any());
        verify(reviewEventsQueueClient, never()).deleteMessage(any(), any());
    }

    @Test
    void pollMessages_queueClientThrowsException_handlesGracefully() {
        doThrow(new RuntimeException("Connection error"))
            .when(reviewEventsQueueClient).receiveMessages(anyInt(), any(), any(), any());

        // Should not throw exception
        reviewMessageConsumer.pollMessages();

        verify(sessionService, never()).processReview(any(), any(), any());
    }

    @Test
    void reviewMessage_serialization_roundTrip() throws Exception {
        // Test that ReviewMessage can be serialized and deserialized correctly
        ReviewMessage original = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Great session!")
            .timestamp(LocalDateTime.of(2024, 1, 15, 10, 30))
            .coachId(2L)
            .build();

        String json = objectMapper.writeValueAsString(original);
        ReviewMessage deserialized = objectMapper.readValue(json, ReviewMessage.class);

        assert deserialized.getSessionId().equals(1L);
        assert deserialized.getRating().compareTo(BigDecimal.valueOf(8.5)) == 0;
        assert deserialized.getComment().equals("Great session!");
        assert deserialized.getCoachId().equals(2L);
    }

    @Test
    void reviewMessage_jsonSerialization_roundTrip() throws Exception {
        ReviewMessage original = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Test comment")
            .timestamp(LocalDateTime.now())
            .coachId(1L)
            .build();

        String json = objectMapper.writeValueAsString(original);

        // Simulate what happens in processMessage - direct JSON parsing
        ReviewMessage deserialized = objectMapper.readValue(json, ReviewMessage.class);

        assert deserialized.getSessionId().equals(original.getSessionId());
        assert deserialized.getRating().compareTo(original.getRating()) == 0;
        assert deserialized.getComment().equals(original.getComment());
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

        assert deserialized.getComment() == null;
    }
}
