package org.chamsoft.smarthome.sceneorchestrator.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(new ObjectMapper());

    @Test
    void handleNotFound_returns404() {
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(new ResourceNotFoundException("missing"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
    }

    @Test
    void handleInvalidInput_returns400() {
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidInput(new InvalidInputException("bad"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
    }

    @Test
    void handleInvariantViolation_returns422() {
        ResponseEntity<Map<String, Object>> response = handler.handleInvariantViolation(new SceneInvariantViolationException("broken"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).containsEntry("status", 422);
        assertThat(response.getBody()).containsEntry("message", "broken");
    }

    @Test
    void handleValidation_returnsFirstFieldErrorMessage() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "name is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "name: name is required");
    }

    @Test
    void handleDownstreamHttpError_extractsJsonMessageIfPresent() {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "{\"message\":\"Scene not found\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        ResponseEntity<Map<String, Object>> response = handler.handleDownstreamHttpError(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "Scene not found");
    }

    @Test
    void handleDownstreamHttpError_whenBodyNotJson_returnsRawBody() {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                null,
                "not-json".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        ResponseEntity<Map<String, Object>> response = handler.handleDownstreamHttpError(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "not-json");
    }

    @Test
    void handleDownstreamConnectionError_returns503() {
        ResponseEntity<Map<String, Object>> response = handler.handleDownstreamConnectionError(new ResourceAccessException("down"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("status", 503);
    }

    @Test
    void handleGeneric_returns500() {
        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(new HttpMessageNotReadableException("boom"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
    }
}

