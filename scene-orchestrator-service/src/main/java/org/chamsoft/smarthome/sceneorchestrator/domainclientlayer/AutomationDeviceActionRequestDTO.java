package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutomationDeviceActionRequestDTO {
    private UUID deviceId;
    private String actionType;
    private String parameters;
}
