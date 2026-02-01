package com.kulebiakin.common.queue;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Azure Service Bus clients.
 * Only enabled when azure.servicebus.connection-string is set.
 */
@Configuration
@ConditionalOnProperty(name = "azure.servicebus.connection-string")
public class ServiceBusConfig {

    @Value("${azure.servicebus.connection-string}")
    private String connectionString;

    @Value("${azure.servicebus.queue-name:reviews-queue}")
    private String queueName;

    @Bean
    public ServiceBusSenderClient serviceBusSenderClient() {
        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient();
    }

    @Bean
    public String serviceBusQueueName() {
        return queueName;
    }

    @Bean
    public String serviceBusConnectionString() {
        return connectionString;
    }
}
