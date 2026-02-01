package com.kulebiakin.sessionservice.messaging;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulebiakin.common.dto.ReviewMessage;
import com.kulebiakin.sessionservice.service.SessionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumes review messages from Azure Service Bus queue using push-based processing.
 */
@Component
@ConditionalOnProperty(name = "azure.servicebus.connection-string")
@RequiredArgsConstructor
@Slf4j
public class ReviewMessageConsumer {

    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    @Value("${azure.servicebus.connection-string}")
    private String connectionString;

    @Value("${azure.servicebus.queue-name:reviews-queue}")
    private String queueName;

    private ServiceBusProcessorClient processorClient;

    @PostConstruct
    public void start() {
        log.info("Starting Service Bus processor for queue: {}", queueName);

        processorClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .processor()
                .queueName(queueName)
                .processMessage(this::processMessage)
                .processError(this::processError)
                .buildProcessorClient();

        processorClient.start();
        log.info("Service Bus processor started successfully");
    }

    @PreDestroy
    public void stop() {
        if (processorClient != null) {
            log.info("Stopping Service Bus processor");
            processorClient.close();
        }
    }

    private void processMessage(ServiceBusReceivedMessageContext context) {
        String messageId = context.getMessage().getMessageId();
        try {
            String payload = context.getMessage().getBody().toString();
            log.info("Received review message from Service Bus: {}", payload);

            ReviewMessage reviewMessage = objectMapper.readValue(payload, ReviewMessage.class);

            sessionService.processReview(
                    reviewMessage.getSessionId(),
                    reviewMessage.getRating(),
                    reviewMessage.getComment()
            );

            context.complete();
            log.info("Successfully processed message for session {}", reviewMessage.getSessionId());

        } catch (Exception e) {
            log.error("Error processing review message {}: {}", messageId, e.getMessage(), e);
            context.abandon();
        }
    }

    private void processError(ServiceBusErrorContext context) {
        log.error("Service Bus error: {} - {}",
                context.getErrorSource(),
                context.getException().getMessage());
    }
}
