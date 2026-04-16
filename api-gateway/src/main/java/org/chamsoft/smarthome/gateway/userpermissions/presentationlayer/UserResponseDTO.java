package org.chamsoft.smarthome.gateway.userpermissions.presentationlayer;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserResponseDTO {

    private UUID userId;
    private String username;
    private String email;
    private String role;
    private String accountStatus;

    private List<PermissionDTO> permissions;
}
