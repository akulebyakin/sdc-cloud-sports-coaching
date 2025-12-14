package com.kulebiakin.reviewservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.common.exception.MessageProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewMessageProducer {

    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    private static final String OUTPUT_BINDING = "supply-out-0";

    public void sendReviewMessage(ReviewMessage reviewMessage) {
        try {
            String messageJson = objectMapper.writeValueAsString(reviewMessage);
            log.info("Sending review message to Service Bus: {}", messageJson);

            boolean sent = streamBridge.send(OUTPUT_BINDING, messageJson);

            if (sent) {
                log.info("Successfully sent review message for session {}", reviewMessage.getSessionId());
            } else {
                log.error("Failed to send review message for session {}", reviewMessage.getSessionId());
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing review message: {}", e.getMessage(), e);
            throw new MessageProcessingException("Failed to serialize review message", e);
        }
    }
}
