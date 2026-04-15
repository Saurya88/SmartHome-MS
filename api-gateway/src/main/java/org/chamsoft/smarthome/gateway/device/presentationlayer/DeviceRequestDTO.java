package org.chamsoft.smarthome.gateway.device.presentationlayer;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRequestDTO {

    private String deviceName;
    private String deviceType;
    private String firmwareVersion;
    private List<String> capabilities;
    private String connectivityType;
}