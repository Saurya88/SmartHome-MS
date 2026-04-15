package org.chamsoft.smarthome.userpermissions.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    private String username;
    private String email;
    private String role;

    // Permissions assigned to the user
    private List<PermissionDTO> permissions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PermissionDTO {

        private UUID deviceId;
        private String accessLevel;

    }
}