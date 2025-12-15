package com.kulebiakin.reviewservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.common.exception.MessageProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewMessageProducerTest {

    @Mock
    private StreamBridge streamBridge;

    @Mock
    private ObjectMapper objectMapper;

    private ReviewMessageProducer reviewMessageProducer;

    @BeforeEach
    void setUp() {
        reviewMessageProducer = new ReviewMessageProducer(streamBridge, objectMapper);
    }

    @Test
    void sendReviewMessage_success_sendsToStreamBridge() throws Exception {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Great session!")
            .coachId(1L)
            .timestamp(LocalDateTime.now())
            .build();

        String jsonMessage = "{\"sessionId\":1,\"rating\":8.5}";
        when(objectMapper.writeValueAsString(reviewMessage)).thenReturn(jsonMessage);
        when(streamBridge.send("supply-out-0", jsonMessage)).thenReturn(true);

        reviewMessageProducer.sendReviewMessage(reviewMessage);

        verify(streamBridge).send("supply-out-0", jsonMessage);
    }

    @Test
    void sendReviewMessage_serializationError_throwsMessageProcessingException() throws Exception {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .coachId(1L)
            .build();

        when(objectMapper.writeValueAsString(reviewMessage))
            .thenThrow(new JsonProcessingException("Serialization error") {
            });

        assertThatThrownBy(() -> reviewMessageProducer.sendReviewMessage(reviewMessage))
            .isInstanceOf(MessageProcessingException.class);
    }

    @Test
    void sendReviewMessage_streamBridgeReturnsFalse_logsError() throws Exception {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .coachId(1L)
            .build();

        String jsonMessage = "{\"sessionId\":1}";
        when(objectMapper.writeValueAsString(reviewMessage)).thenReturn(jsonMessage);
        when(streamBridge.send("supply-out-0", jsonMessage)).thenReturn(false);

        // Should not throw, just log error
        assertThatCode(() -> reviewMessageProducer.sendReviewMessage(reviewMessage))
            .doesNotThrowAnyException();
        verify(streamBridge).send("supply-out-0", jsonMessage);
    }

    @Test
    void sendReviewMessage_usesCorrectOutputBinding() throws Exception {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(9.0))
            .coachId(1L)
            .build();

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(streamBridge.send(any(), any())).thenReturn(true);

        reviewMessageProducer.sendReviewMessage(reviewMessage);

        verify(streamBridge).send(eq("supply-out-0"), any());
    }
}
