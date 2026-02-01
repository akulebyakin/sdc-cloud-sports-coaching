package com.kulebiakin.reviewservice.messaging;

import com.azure.core.util.BinaryData;
import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.common.exception.MessageProcessingException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sends review messages to Azure Storage Queue.
 * Uses BinaryData for proper message encoding.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewMessageProducer {

    private final QueueClient reviewEventsQueueClient;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        // Ensure queue exists
        try {
            reviewEventsQueueClient.createIfNotExists();
            log.info("Queue ready: {}", reviewEventsQueueClient.getQueueName());
        } catch (Exception e) {
            log.warn("Could not create queue (may already exist): {}", e.getMessage());
        }
    }

    public void sendReviewMessage(ReviewMessage reviewMessage) {
        try {
            String messageJson = objectMapper.writeValueAsString(reviewMessage);

            log.info("Sending review message to Storage Queue: {}", messageJson);

            // Use BinaryData for proper encoding
            BinaryData binaryData = BinaryData.fromString(messageJson);
            var result = reviewEventsQueueClient.sendMessage(binaryData);

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
