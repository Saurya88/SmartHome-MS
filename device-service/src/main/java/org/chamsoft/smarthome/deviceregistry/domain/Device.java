package org.chamsoft.smarthome.deviceregistry.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @Column(name = "device_id", columnDefinition = "VARCHAR(36)")
    @Builder.Default
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID deviceId = UUID.randomUUID();

    private String deviceName;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    private String firmwareVersion;

    private LocalDateTime registeredAt;

    @ElementCollection
    @CollectionTable(name = "device_capabilities", joinColumns = @JoinColumn(name = "device_id"))
    @Column(name = "capability")
    private List<String> capabilities;

    private String connectivityType;
}