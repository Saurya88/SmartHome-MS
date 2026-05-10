package org.chamsoft.smarthome.sceneorchestrator.presentationlayer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "ownerUserId is required")
    private UUID ownerUserId;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "status is required")
    private String status;

    @Valid
    @NotEmpty(message = "deviceActions must contain at least one entry")
    private List<DeviceActionRequestDTO> deviceActions;

    @Valid
    @NotEmpty(message = "triggerRules must contain at least one entry")
    private List<TriggerRuleRequestDTO> triggerRules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceActionRequestDTO {
        @NotNull(message = "deviceId is required")
        private UUID deviceId;
        @NotBlank(message = "actionType is required")
        private String actionType;
        private String parameters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerRuleRequestDTO {
        @NotBlank(message = "triggerId is required")
        private String triggerId;
        @NotBlank(message = "triggerType is required")
        private String triggerType;
        @NotBlank(message = "conditionExpression is required")
        private String conditionExpression;
        private boolean enabled;
        private String triggerValue;
    }
}

