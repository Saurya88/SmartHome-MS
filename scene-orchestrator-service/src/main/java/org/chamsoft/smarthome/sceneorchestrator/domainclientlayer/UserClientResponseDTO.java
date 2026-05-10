package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserClientResponseDTO {
    private UUID userId;
    private String accountStatus;
    private List<PermissionClientDTO> permissions;

    @Data
    public static class PermissionClientDTO {
        private UUID deviceId;
        private String accessLevel;
    }
}

