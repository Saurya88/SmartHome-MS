package org.chamsoft.smarthome.deviceregistry.utilities;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returnsNotFoundResponse() {
        var response = handler.handleNotFound(new ResourceNotFoundException("missing"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "missing");
    }

    @Test
    void handleConflict_returnsConflictResponse() {
        var response = handler.handleConflict(new BusinessRuleViolationException("conflict"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "conflict");
    }

    @Test
    void handleBadRequest_returnsBadRequestResponse() {
        var response = handler.handleBadRequest(new IllegalArgumentException("bad request"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "bad request");
    }

    @Test
    void handleIllegalState_returnsConflictResponse() {
        var response = handler.handleIllegalState(new IllegalStateException("illegal state"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "illegal state");
    }

    @Test
    void handleGeneric_returnsInternalServerErrorResponse() {
        var response = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred");
    }
}
