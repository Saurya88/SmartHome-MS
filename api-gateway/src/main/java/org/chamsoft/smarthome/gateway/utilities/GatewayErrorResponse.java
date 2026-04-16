package org.chamsoft.smarthome.gateway.utilities;

import java.time.LocalDateTime;

public record GatewayErrorResponse(
        String timestamp,
        int status,
        String error,
        String message
) {
    public static GatewayErrorResponse of(int status, String error, String message) {
        return new GatewayErrorResponse(LocalDateTime.now().toString(), status, error, message);
    }
}

