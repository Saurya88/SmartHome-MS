package org.chamsoft.smarthome.userpermissions.domain;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User {

    @Id
    @Column(name = "user_id", columnDefinition = "VARCHAR(36)")
    @Builder.Default
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID userId = UUID.randomUUID();

    @Column(name = "username", nullable = false)
    private String username;

    @Embedded
    private Email email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @ElementCollection
    @CollectionTable(name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private List<Permission> permissions = new ArrayList<>();

}