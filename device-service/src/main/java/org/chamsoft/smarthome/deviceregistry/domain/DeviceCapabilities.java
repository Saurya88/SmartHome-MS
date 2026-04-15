package org.chamsoft.smarthome.deviceregistry.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.List;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCapabilities {

    private String capabilityType;

    @ElementCollection
    private List<String> supportedActions;

    private String unit;
}