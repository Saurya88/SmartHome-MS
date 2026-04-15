package org.chamsoft.smarthome.gateway.automation.presentationlayer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceActionDTO {
    private UUID deviceId;
    private String deviceName;
    private String deviceType;
    private String deviceStatus;
    private String actionType;
    private String parameters;
}
