package org.chamsoft.smarthome.automation.presentationlayer;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerRuleRequestDTO {
    @NotBlank(message = "triggerId is required")
    private String triggerId;

    @NotBlank(message = "triggerType is required")
    private String triggerType;

    @NotBlank(message = "conditionExpression is required")
    private String conditionExpression;

    private boolean enabled;

    private String triggerValue;
}
