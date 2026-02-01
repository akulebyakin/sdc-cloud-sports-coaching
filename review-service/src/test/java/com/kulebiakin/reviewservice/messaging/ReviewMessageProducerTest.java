package com.kulebiakin.reviewservice.messaging;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewMessageProducerTest {

    @Mock
    private ServiceBusSenderClient serviceBusSenderClient;

    private ObjectMapper objectMapper;
    private ReviewMessageProducer reviewMessageProducer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        reviewMessageProducer = new ReviewMessageProducer(serviceBusSenderClient, objectMapper);
    }

    @Test
    void sendReviewMessage_success_sendsToServiceBus() {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Great session!")
            .coachId(1L)
            .timestamp(LocalDateTime.now())
            .build();

        reviewMessageProducer.sendReviewMessage(reviewMessage);

        verify(serviceBusSenderClient).sendMessage(any(ServiceBusMessage.class));
    }

    @Test
    void sendReviewMessage_serviceBusError_throwsMessageProcessingException() {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .coachId(1L)
            .timestamp(LocalDateTime.now())
            .build();

        doThrow(new RuntimeException("Connection error"))
            .when(serviceBusSenderClient).sendMessage(any(ServiceBusMessage.class));

        assertThatThrownBy(() -> reviewMessageProducer.sendReviewMessage(reviewMessage))
            .isInstanceOf(MessageProcessingException.class)
            .hasMessageContaining("Failed to send review message");
    }

    @Test
    void sendReviewMessage_nullComment_succeeds() {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(7.0))
            .comment(null)
            .coachId(1L)
            .timestamp(LocalDateTime.now())
            .build();

        assertThatCode(() -> reviewMessageProducer.sendReviewMessage(reviewMessage))
            .doesNotThrowAnyException();

        verify(serviceBusSenderClient).sendMessage(any(ServiceBusMessage.class));
    }
}
