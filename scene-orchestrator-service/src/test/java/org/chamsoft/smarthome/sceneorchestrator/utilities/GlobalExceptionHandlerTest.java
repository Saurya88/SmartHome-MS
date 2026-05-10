package org.chamsoft.smarthome.sceneorchestrator.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(new ObjectMapper());

    @Test
    void handleInvalidInput_returnsBadRequestResponse() {
        var response = handler.handleInvalidInput(new InvalidInputException("bad input"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "bad input");
    }

    @Test
    void handleInvariantViolation_returnsConflictResponse() {
        var response = handler.handleInvariantViolation(new SceneInvariantViolationException("invariant"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "invariant");
    }

    @Test
    void handleDownstreamHttpError_returnsDownstreamStatusAndMessage() {
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "{\"message\":\"User not found\"}".getBytes(),
                null
        );

        var response = handler.handleDownstreamHttpError(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "User not found");
    }

    @Test
    void handleDownstreamConnectionError_returnsServiceUnavailable() {
        var response = handler.handleDownstreamConnectionError(new ResourceAccessException("offline"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("message", "A downstream service is unavailable");
    }

    @Test
    void handleGeneric_returnsInternalServerError() {
        var response = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred");
    }
}
