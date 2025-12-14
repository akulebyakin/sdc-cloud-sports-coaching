package com.kulebiakin.sessionservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.common.exception.MessageProcessingException;
import com.kulebiakin.sessionservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ReviewMessageConsumer {

    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    @Bean
    public Consumer<Message<String>> consume() {
        return message -> {
            try {
                String payload = message.getPayload();
                log.info("Received review message: {}", payload);

                ReviewMessage reviewMessage = objectMapper.readValue(payload, ReviewMessage.class);

                // Process the review - update session rating, comment, and notify coach service
                sessionService.processReview(
                        reviewMessage.getSessionId(),
                        reviewMessage.getRating(),
                        reviewMessage.getComment()
                );

                log.info("Successfully processed review for session {}", reviewMessage.getSessionId());
            } catch (Exception e) {
                log.error("Error processing review message: {}", e.getMessage(), e);
                throw new MessageProcessingException("Failed to process review message", e);
            }
        };
    }
}
