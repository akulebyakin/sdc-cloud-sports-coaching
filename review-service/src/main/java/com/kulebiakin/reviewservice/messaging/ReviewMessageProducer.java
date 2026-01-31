package com.kulebiakin.reviewservice.messaging;

import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.common.exception.MessageProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Sends review messages to Azure Storage Queue.
 * Messages are Base64 encoded as required by Azure Storage Queue.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewMessageProducer {

    private final QueueClient reviewEventsQueueClient;
    private final ObjectMapper objectMapper;

    public void sendReviewMessage(ReviewMessage reviewMessage) {
        try {
            String messageJson = objectMapper.writeValueAsString(reviewMessage);
            // Azure Storage Queue requires Base64 encoding for message content
            String encodedMessage = Base64.getEncoder().encodeToString(messageJson.getBytes());

            log.info("Sending review message to Storage Queue: {}", messageJson);

            var result = reviewEventsQueueClient.sendMessage(encodedMessage);

            log.info("Successfully sent review message for session {}, messageId: {}",
                    reviewMessage.getSessionId(), result.getMessageId());

        } catch (JsonProcessingException e) {
            log.error("Error serializing review message: {}", e.getMessage(), e);
            throw new MessageProcessingException("Failed to serialize review message", e);
        } catch (Exception e) {
            log.error("Error sending message to queue: {}", e.getMessage(), e);
            throw new MessageProcessingException("Failed to send review message to queue", e);
        }
    }
}
