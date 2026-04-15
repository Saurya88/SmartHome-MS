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
public class MaintenanceRecord {
    private LocalDateTime maintenanceDate;
    private String performedBy;
    private String notes;
}