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
    void handleDownstreamHttpError_whenBodyContainsMessage_extractsIt() {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "{\"message\":\"Device not found\"}".getBytes(),
                null
        );

        GatewayErrorResponse response = handler.handleDownstreamHttpError(ex).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.message()).isEqualTo("Device not found");
    }

    @Test
    void handleDownstreamConnectionError_returnsServiceUnavailable() {
        GatewayErrorResponse response = handler.handleDownstreamConnectionError(new ResourceAccessException("down")).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(503);
    }

    @Test
    void handleUnexpectedError_returnsInternalServerError() {
        GatewayErrorResponse response = handler.handleUnexpectedError(new RuntimeException("boom")).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(500);
    }
}
