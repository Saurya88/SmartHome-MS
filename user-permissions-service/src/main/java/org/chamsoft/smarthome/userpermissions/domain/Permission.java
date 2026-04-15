package org.chamsoft.smarthome.userpermissions.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Permission {

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "device_id", columnDefinition = "VARCHAR(36)")
    private UUID deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level")
    private AccessLevel accessLevel;

}