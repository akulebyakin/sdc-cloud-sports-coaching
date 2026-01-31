package com.kulebiakin.common.queue;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Azure Storage Queue clients.
 * Creates beans for review-events and app-logs queues.
 * Only enabled when azure.storage.connection-string is non-empty.
 */
@Configuration
@ConditionalOnExpression("!'${azure.storage.connection-string:}'.isEmpty()")
public class AzureQueueConfig {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.queue.review-events:review-events}")
    private String reviewEventsQueueName;

    @Value("${azure.storage.queue.app-logs:app-logs}")
    private String appLogsQueueName;

    @Bean
    public QueueClient reviewEventsQueueClient() {
        return new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(reviewEventsQueueName)
                .buildClient();
    }

    @Bean
    public QueueClient appLogsQueueClient() {
        return new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(appLogsQueueName)
                .buildClient();
    }
}
