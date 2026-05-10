package org.chamsoft.smarthome.sceneorchestrator.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagedDeviceAction {

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "device_id", columnDefinition = "VARCHAR(36)")
    private UUID deviceId;

    private String actionType;

    private String parameters;
}
