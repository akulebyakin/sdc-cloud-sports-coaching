package com.kulebiakin.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageProcessingExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        String message = "Failed to process message";
        MessageProcessingException exception = new MessageProcessingException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructor_withMessageAndCause_setsMessageAndCause() {
        String message = "Failed to process message";
        Throwable cause = new RuntimeException("Original error");

        MessageProcessingException exception = new MessageProcessingException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void exception_isRuntimeException() {
        MessageProcessingException exception = new MessageProcessingException("test");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
