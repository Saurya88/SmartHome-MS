package org.chamsoft.smarthome.gateway.automation.presentationlayer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceActionRequestDTO {
    @NotNull(message = "deviceId is required")
    private UUID deviceId;

    @NotBlank(message = "actionType is required")
    private String actionType;

    @NotBlank(message = "parameters is required")
    private String parameters;
}
