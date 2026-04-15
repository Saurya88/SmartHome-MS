package org.chamsoft.smarthome.deviceregistry.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {
    private LocalDateTime timestamp;
    private String value;
    private String unit;
}