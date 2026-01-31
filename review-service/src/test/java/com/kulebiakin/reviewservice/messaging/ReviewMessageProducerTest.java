package com.kulebiakin.reviewservice.messaging;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.common.exception.MessageProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewMessageProducerTest {

    @Mock
    private QueueClient reviewEventsQueueClient;

    private ObjectMapper objectMapper;
    private ReviewMessageProducer reviewMessageProducer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        reviewMessageProducer = new ReviewMessageProducer(reviewEventsQueueClient, objectMapper);
    }

    @Test
    void sendReviewMessage_success_sendsToQueue() throws Exception {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Great session!")
            .coachId(1L)
            .timestamp(LocalDateTime.now())
            .build();

        SendMessageResult mockResult = mock(SendMessageResult.class);
        when(mockResult.getMessageId()).thenReturn("msg-123");
        when(reviewEventsQueueClient.sendMessage(anyString())).thenReturn(mockResult);

        reviewMessageProducer.sendReviewMessage(reviewMessage);

        verify(reviewEventsQueueClient).sendMessage(anyString());
    }

    @Test
    void sendReviewMessage_sendsBase64EncodedMessage() throws Exception {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Test")
            .coachId(1L)
            .timestamp(LocalDateTime.now())
            .build();

        SendMessageResult mockResult = mock(SendMessageResult.class);
        when(mockResult.getMessageId()).thenReturn("msg-123");
        when(reviewEventsQueueClient.sendMessage(anyString())).thenAnswer(invocation -> {
            String message = invocation.getArgument(0);
            // Verify it's base64 encoded
            byte[] decoded = Base64.getDecoder().decode(message);
            String json = new String(decoded);
            // If we get here without exception, it's valid base64
            assert json.contains("\"sessionId\"");
            return mockResult;
        });

        reviewMessageProducer.sendReviewMessage(reviewMessage);

        verify(reviewEventsQueueClient).sendMessage(anyString());
    }

    @Test
    void sendReviewMessage_queueError_throwsMessageProcessingException() {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .coachId(1L)
            .timestamp(LocalDateTime.now())
            .build();

        when(reviewEventsQueueClient.sendMessage(anyString()))
            .thenThrow(new RuntimeException("Connection error"));

        assertThatThrownBy(() -> reviewMessageProducer.sendReviewMessage(reviewMessage))
            .isInstanceOf(MessageProcessingException.class)
            .hasMessageContaining("Failed to send review message");
    }

    @Test
    void sendReviewMessage_nullComment_succeeds() throws Exception {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(7.0))
            .comment(null)
            .coachId(1L)
            .timestamp(LocalDateTime.now())
            .build();

        SendMessageResult mockResult = mock(SendMessageResult.class);
        when(mockResult.getMessageId()).thenReturn("msg-456");
        when(reviewEventsQueueClient.sendMessage(anyString())).thenReturn(mockResult);

        assertThatCode(() -> reviewMessageProducer.sendReviewMessage(reviewMessage))
            .doesNotThrowAnyException();

        verify(reviewEventsQueueClient).sendMessage(anyString());
    }
}
