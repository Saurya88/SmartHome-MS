package org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SceneOrchestrationResponseDTO {
    private UUID orchestrationId;
    private UUID ownerUserId;
    private UUID automationSceneId;
    private String name;
    private String status;
    private LocalDateTime createdAt;
    private List<DeviceActionResponseDTO> deviceActions;
    private List<TriggerRuleResponseDTO> triggerRules;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeviceActionResponseDTO {
        private UUID deviceId;
        private String actionType;
        private String parameters;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TriggerRuleResponseDTO {
        private String triggerId;
        private String triggerType;
        private String conditionExpression;
        private boolean enabled;
        private String triggerValue;
    }
}
