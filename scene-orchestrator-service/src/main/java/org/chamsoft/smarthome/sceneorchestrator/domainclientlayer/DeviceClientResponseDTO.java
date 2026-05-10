package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.Data;

import java.util.UUID;

@Data
public class DeviceClientResponseDTO {
    private UUID deviceId;
    private String deviceName;
    private String status;
    private String firmwareVersion;
}

