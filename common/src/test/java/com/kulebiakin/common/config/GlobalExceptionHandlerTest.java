package com.kulebiakin.common.config;

import com.kulebiakin.common.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleEntityNotFoundException_returnsNotFoundStatus() {
        EntityNotFoundException exception = new EntityNotFoundException("Entity with id 1 not found");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleEntityNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("Entity with id 1 not found");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void handleResourceNotFoundException_returnsNotFoundStatus() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Coach", 42L);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResourceNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat((String) response.getBody().get("message")).contains("Coach");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void handleValidationExceptions_returnsBadRequestWithFieldErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "firstName", "must not be blank");
        FieldError fieldError2 = new FieldError("object", "email", "must be a valid email");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error")).isEqualTo("Validation Failed");
        assertThat(response.getBody()).containsKey("details");
        assertThat(response.getBody()).containsKey("timestamp");

        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertThat(details).containsEntry("firstName", "must not be blank");
        assertThat(details).containsEntry("email", "must be a valid email");
    }

    @Test
    void handleValidationExceptions_handlesSingleFieldError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("object", "rating", "must be between 0 and 10");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertThat(details).hasSize(1);
        assertThat(details).containsEntry("rating", "must be between 0 and 10");
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        Exception exception = new RuntimeException("Unexpected database error");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("Unexpected database error");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void handleGenericException_handlesNullPointerException() {
        NullPointerException exception = new NullPointerException("null reference");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
    }

    @Test
    void handleGenericException_handlesIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter value");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("message")).isEqualTo("Invalid parameter value");
    }

    @Test
    void allHandlers_includeTimestampInResponse() {
        EntityNotFoundException entityEx = new EntityNotFoundException("test");
        ResourceNotFoundException resourceEx = new ResourceNotFoundException("Test", 1L);
        RuntimeException genericEx = new RuntimeException("test");

        assertThat(exceptionHandler.handleEntityNotFoundException(entityEx).getBody())
            .containsKey("timestamp");
        assertThat(exceptionHandler.handleResourceNotFoundException(resourceEx).getBody())
            .containsKey("timestamp");
        assertThat(exceptionHandler.handleGenericException(genericEx).getBody())
            .containsKey("timestamp");
    }

    @Test
    void handleResourceNotFoundException_withDifferentResourceTypes() {
        ResourceNotFoundException coachException = new ResourceNotFoundException("Coach", 1L);
        ResourceNotFoundException sessionException = new ResourceNotFoundException("Session", 2L);
        ResourceNotFoundException userException = new ResourceNotFoundException("User", 3L);

        ResponseEntity<Map<String, Object>> coachResponse =
            exceptionHandler.handleResourceNotFoundException(coachException);
        ResponseEntity<Map<String, Object>> sessionResponse =
            exceptionHandler.handleResourceNotFoundException(sessionException);
        ResponseEntity<Map<String, Object>> userResponse =
            exceptionHandler.handleResourceNotFoundException(userException);

        assertThat(coachResponse.getBody().get("message").toString()).contains("Coach");
        assertThat(sessionResponse.getBody().get("message").toString()).contains("Session");
        assertThat(userResponse.getBody().get("message").toString()).contains("User");
    }
}
