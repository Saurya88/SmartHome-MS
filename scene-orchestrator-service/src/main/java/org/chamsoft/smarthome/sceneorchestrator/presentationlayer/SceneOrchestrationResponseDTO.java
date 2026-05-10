package org.chamsoft.smarthome.sceneorchestrator.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SceneOrchestrationResponseDTO {

    private UUID orchestrationId;
    private UUID ownerUserId;
    private UUID automationSceneId;
    private String name;
    private String status;
    private LocalDateTime createdAt;
    private List<DeviceActionResponseDTO> deviceActions;
    private List<TriggerRuleResponseDTO> triggerRules;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceActionResponseDTO {
        private UUID deviceId;
        private String actionType;
        private String parameters;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TriggerRuleResponseDTO {
        private String triggerId;
        private String triggerType;
        private String conditionExpression;
        private boolean enabled;
        private String triggerValue;
    }
}
