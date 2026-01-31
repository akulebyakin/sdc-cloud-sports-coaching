package com.kulebiakin.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;

import java.time.Instant;
import java.util.Base64;

/**
 * Custom Logback Appender that sends log messages to Azure Storage Queue.
 * Configured via logback-spring.xml with connectionString, queueName, and applicationName.
 */
public class AzureQueueAppender extends AppenderBase<ILoggingEvent> {

    private String connectionString;
    private String queueName = "app-logs";
    private String applicationName = "unknown";
    private QueueClient queueClient;

    @Override
    public void start() {
        if (connectionString == null || connectionString.isEmpty()) {
            addWarn("Azure Storage connection string not configured, queue logging disabled");
            return;
        }
        try {
            queueClient = new QueueClientBuilder()
                    .connectionString(connectionString)
                    .queueName(queueName)
                    .buildClient();
            super.start();
            addInfo("Azure Queue Appender started for queue: " + queueName);
        } catch (Exception e) {
            addError("Failed to initialize Azure Queue client", e);
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (queueClient == null) {
            return;
        }

        try {
            String json = String.format(
                    "{\"timestamp\":\"%s\",\"level\":\"%s\",\"logger\":\"%s\",\"message\":\"%s\",\"application\":\"%s\",\"thread\":\"%s\"}",
                    Instant.ofEpochMilli(event.getTimeStamp()).toString(),
                    event.getLevel().toString(),
                    event.getLoggerName(),
                    escapeJson(event.getFormattedMessage()),
                    applicationName,
                    event.getThreadName()
            );

            String encodedMessage = Base64.getEncoder().encodeToString(json.getBytes());
            queueClient.sendMessage(encodedMessage);
        } catch (Exception e) {
            addError("Failed to send log message to Azure Storage Queue", e);
        }
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    // Setters for Logback configuration
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
