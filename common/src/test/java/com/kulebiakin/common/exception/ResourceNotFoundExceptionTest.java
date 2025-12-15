package com.kulebiakin.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void constructor_withResourceNameAndId_formatsMessage() {
        String resourceName = "Coach";
        Long id = 123L;

        ResourceNotFoundException exception = new ResourceNotFoundException(resourceName, id);

        assertThat(exception.getMessage()).isEqualTo("Coach not found with id: 123");
    }

    @Test
    void exception_isRuntimeException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("test");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
