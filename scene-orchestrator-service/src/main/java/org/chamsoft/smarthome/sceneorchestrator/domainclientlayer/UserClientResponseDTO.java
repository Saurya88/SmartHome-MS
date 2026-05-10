package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserClientResponseDTO {
    private UUID userId;
    private String accountStatus;
    private List<PermissionClientDTO> permissions;

    @Getter
    @Setter
    public static class PermissionClientDTO {
        private UUID deviceId;
        private String accessLevel;
    }
}
