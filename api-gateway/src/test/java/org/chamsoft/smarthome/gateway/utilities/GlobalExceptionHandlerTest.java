package org.chamsoft.smarthome.gateway.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(new ObjectMapper());

    @Test
    void handleDownstreamHttpError_returnsStatusAndMessage() {
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "{\"message\":\"Scene not found\"}".getBytes(),
                null
        );

        var response = handler.handleDownstreamHttpError(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("Scene not found");
    }

    @Test
    void handleDownstreamConnectionError_returnsServiceUnavailable() {
        var response = handler.handleDownstreamConnectionError(new ResourceAccessException("offline"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().message()).isEqualTo("A downstream service is unavailable");
    }

    @Test
    void handleUnexpectedError_returnsInternalServerError() {
        var response = handler.handleUnexpectedError(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("An unexpected gateway error occurred");
    }
}
