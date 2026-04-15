package org.chamsoft.smarthome.deviceregistry.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkInfo {
    private String ipAddress;
    private String macAddress;

    @Enumerated(EnumType.STRING)
    private ConnectivityType connectivityType;
}