package org.chamsoft.smarthome.gateway.device.presentationlayer;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponseDTO {

    private UUID deviceId;
    private String deviceName;
    private String deviceType;
    private String status;
    private String firmwareVersion;
    private LocalDateTime registeredAt;
    private List<String> capabilities;
    private String connectivityType;
}