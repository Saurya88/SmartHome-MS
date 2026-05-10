package org.chamsoft.smarthome.gateway.device.presentationlayer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRequestDTO {

    @NotBlank(message = "deviceName is required")
    private String deviceName;

    @NotBlank(message = "deviceType is required")
    private String deviceType;

    @Pattern(
            regexp = "ONLINE|OFFLINE|MAINTENANCE",
            message = "status must be ONLINE, OFFLINE, or MAINTENANCE"
    )
    private String status;

    @NotBlank(message = "firmwareVersion is required")
    private String firmwareVersion;

    private List<String> capabilities;

    @NotBlank(message = "connectivityType is required")
    private String connectivityType;
}
