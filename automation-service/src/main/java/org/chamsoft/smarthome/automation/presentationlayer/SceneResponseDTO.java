package org.chamsoft.smarthome.automation.presentationlayer;

import lombok.Getter;
import lombok.Setter;
import org.chamsoft.smarthome.automation.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SceneResponseDTO {

    private UUID sceneId;
    private String name;
    private SceneStatus status;
    private LocalDateTime createdAt;
    private List<TriggerRuleResponseDTO> triggerRules;
    private List<DeviceActionDTO> deviceActions;

    @Getter
    @Setter
    public static class DeviceActionDTO {
        private UUID deviceId;
        private String deviceName;
        private String deviceType;
        private String deviceStatus;
        private String actionType;
        private String parameters;
    }
}
