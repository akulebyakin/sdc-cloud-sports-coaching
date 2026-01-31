package com.kulebiakin.sessionservice.messaging;

import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.sessionservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Base64;

/**
 * Polls Azure Storage Queue for review messages and processes them.
 * Uses @Scheduled for periodic polling instead of push-based Service Bus.
 */
@Component
@EnableScheduling
@ConditionalOnExpression("!'${azure.storage.connection-string:}'.isEmpty()")
@RequiredArgsConstructor
@Slf4j
public class ReviewMessageConsumer {

    private final QueueClient reviewEventsQueueClient;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    @Value("${azure.storage.queue.visibility-timeout-seconds:30}")
    private int visibilityTimeoutSeconds;

    @Value("${azure.storage.queue.max-messages:10}")
    private int maxMessages;

    /**
     * Polls the review-events queue for new messages.
     * Default interval is 5 seconds, configurable via azure.storage.queue.poll-interval-ms
     */
    @Scheduled(fixedDelayString = "${azure.storage.queue.poll-interval-ms:5000}")
    public void pollMessages() {
        log.debug("Polling review-events queue...");

        try {
            Iterable<QueueMessageItem> messages = reviewEventsQueueClient.receiveMessages(
                    maxMessages,
                    Duration.ofSeconds(visibilityTimeoutSeconds),
                    Duration.ofSeconds(30),
                    Context.NONE
            );

            for (QueueMessageItem message : messages) {
                processMessage(message);
            }
        } catch (Exception e) {
            log.error("Error polling messages from queue: {}", e.getMessage(), e);
        }
    }

    private void processMessage(QueueMessageItem message) {
        String messageId = message.getMessageId();
        String popReceipt = message.getPopReceipt();

        try {
            // Decode Base64 message
            String decodedPayload = new String(
                    Base64.getDecoder().decode(message.getBody().toString())
            );
            log.info("Received review message: {}", decodedPayload);

            ReviewMessage reviewMessage = objectMapper.readValue(decodedPayload, ReviewMessage.class);

            // Process the review - update session rating, comment, and notify coach service
            sessionService.processReview(
                    reviewMessage.getSessionId(),
                    reviewMessage.getRating(),
                    reviewMessage.getComment()
            );

            // Delete message from queue after successful processing
            reviewEventsQueueClient.deleteMessage(messageId, popReceipt);

            log.info("Successfully processed and deleted message for session {}",
                    reviewMessage.getSessionId());

        } catch (Exception e) {
            log.error("Error processing review message {}: {}", messageId, e.getMessage(), e);
            // Message will become visible again after visibility timeout for retry
        }
    }
}
