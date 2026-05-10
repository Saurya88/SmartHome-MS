package org.chamsoft.smarthome.automation.presentationlayer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceActionRequestDTO {

    @NotNull(message = "deviceId is required")
    private UUID deviceId;

    @NotBlank(message = "actionType is required")
    private String actionType;

    @NotBlank(message = "parameters is required")
    private String parameters;
}
