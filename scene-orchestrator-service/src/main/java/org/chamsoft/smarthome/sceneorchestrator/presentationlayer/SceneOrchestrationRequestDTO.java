package org.chamsoft.smarthome.sceneorchestrator.presentationlayer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SceneOrchestrationRequestDTO {

    @NotNull(message = "ownerUserId is required")
    private UUID ownerUserId;

    @NotBlank(message = "name is required")
    private String name;

    @Pattern(
            regexp = "ACTIVE|INACTIVE|SCHEDULED",
            message = "status must be ACTIVE, INACTIVE, or SCHEDULED"
    )
    private String status;

    @Valid
    @NotEmpty(message = "deviceActions must contain at least one entry")
    private List<DeviceActionRequestDTO> deviceActions;

    @Valid
    @NotEmpty(message = "triggerRules must contain at least one entry")
    private List<TriggerRuleRequestDTO> triggerRules;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceActionRequestDTO {

        @NotNull(message = "deviceId is required")
        private UUID deviceId;

        @NotBlank(message = "actionType is required")
        private String actionType;

        @NotBlank(message = "parameters is required")
        private String parameters;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
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
