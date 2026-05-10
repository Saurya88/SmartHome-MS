package org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneOrchestrationRequestDTO {

    private UUID ownerUserId;
    private String name;
    private String status;
    private List<DeviceActionRequestDTO> deviceActions;
    private List<TriggerRuleRequestDTO> triggerRules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceActionRequestDTO {
        private UUID deviceId;
        private String actionType;
        private String parameters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerRuleRequestDTO {
        private String triggerId;
        private String triggerType;
        private String conditionExpression;
        private boolean enabled;
        private String triggerValue;
    }
}

