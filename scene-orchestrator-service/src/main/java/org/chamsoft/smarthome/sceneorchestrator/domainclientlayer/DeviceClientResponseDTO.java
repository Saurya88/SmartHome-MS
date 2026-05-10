package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DeviceClientResponseDTO {
    private UUID deviceId;
    private String deviceName;
    private String status;
    private String firmwareVersion;
}
