package org.chamsoft.smarthome.sceneorchestrator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagedDeviceAction {
    private UUID deviceId;
    private String actionType;
    private String parameters;
}

