package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationDeviceActionRequestDTO {
    private UUID deviceId;
    private String actionType;
    private String parameters;
}

