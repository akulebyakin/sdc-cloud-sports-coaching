package com.kulebiakin.sessionservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.common.exception.MessageProcessingException;
import com.kulebiakin.sessionservice.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewMessageConsumerTest {

    @Mock
    private SessionService sessionService;

    private ObjectMapper objectMapper;
    private ReviewMessageConsumer reviewMessageConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        reviewMessageConsumer = new ReviewMessageConsumer(sessionService, objectMapper);
    }

    @Test
    void consume_validMessage_processesReview() throws Exception {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Great session!")
            .timestamp(LocalDateTime.now())
            .coachId(1L)
            .build();

        String payload = objectMapper.writeValueAsString(reviewMessage);
        Message<String> message = MessageBuilder.withPayload(payload).build();

        Consumer<Message<String>> consumer = reviewMessageConsumer.consume();
        consumer.accept(message);

        verify(sessionService).processReview(1L, BigDecimal.valueOf(8.5), "Great session!");
    }

    @Test
    void consume_invalidJson_throwsMessageProcessingException() {
        String invalidPayload = "invalid json";
        Message<String> message = MessageBuilder.withPayload(invalidPayload).build();

        Consumer<Message<String>> consumer = reviewMessageConsumer.consume();

        assertThatThrownBy(() -> consumer.accept(message))
            .isInstanceOf(MessageProcessingException.class);
    }

    @Test
    void consume_serviceThrowsException_throwsMessageProcessingException() throws Exception {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(8.5))
            .comment("Test")
            .timestamp(LocalDateTime.now())
            .coachId(1L)
            .build();

        String payload = objectMapper.writeValueAsString(reviewMessage);
        Message<String> message = MessageBuilder.withPayload(payload).build();

        doThrow(new RuntimeException("Service error"))
            .when(sessionService).processReview(any(), any(), any());

        Consumer<Message<String>> consumer = reviewMessageConsumer.consume();

        assertThatThrownBy(() -> consumer.accept(message))
            .isInstanceOf(MessageProcessingException.class);
    }

    @Test
    void consume_nullComment_processesReviewWithNullComment() throws Exception {
        ReviewMessage reviewMessage = ReviewMessage.builder()
            .sessionId(1L)
            .rating(BigDecimal.valueOf(7.0))
            .comment(null)
            .timestamp(LocalDateTime.now())
            .coachId(1L)
            .build();

        String payload = objectMapper.writeValueAsString(reviewMessage);
        Message<String> message = MessageBuilder.withPayload(payload).build();

        Consumer<Message<String>> consumer = reviewMessageConsumer.consume();
        consumer.accept(message);

        verify(sessionService).processReview(1L, BigDecimal.valueOf(7.0), null);
    }
}
