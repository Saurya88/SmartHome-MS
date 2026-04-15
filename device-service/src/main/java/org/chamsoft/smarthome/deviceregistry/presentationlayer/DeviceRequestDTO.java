package org.chamsoft.smarthome.deviceregistry.presentationlayer;

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