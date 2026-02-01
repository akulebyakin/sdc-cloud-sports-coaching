package com.kulebiakin.reviewservice.messaging;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.common.exception.MessageProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sends review messages to Azure Service Bus queue.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewMessageProducer {

    private final ServiceBusSenderClient serviceBusSenderClient;
    private final ObjectMapper objectMapper;

    public void sendReviewMessage(ReviewMessage reviewMessage) {
        try {
            String messageJson = objectMapper.writeValueAsString(reviewMessage);
            log.info("Sending review message to Service Bus: {}", messageJson);

            ServiceBusMessage message = new ServiceBusMessage(messageJson);
            message.setContentType("application/json");

            serviceBusSenderClient.sendMessage(message);

            log.info("Successfully sent review message for session {}",
                    reviewMessage.getSessionId());

        } catch (JsonProcessingException e) {
            log.error("Error serializing review message: {}", e.getMessage(), e);
            throw new MessageProcessingException("Failed to serialize review message", e);
        } catch (Exception e) {
            log.error("Error sending message to Service Bus: {}", e.getMessage(), e);
            throw new MessageProcessingException("Failed to send review message to Service Bus", e);
        }
    }
}
