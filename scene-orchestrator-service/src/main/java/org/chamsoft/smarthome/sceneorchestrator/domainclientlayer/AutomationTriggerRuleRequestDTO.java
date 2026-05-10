package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationTriggerRuleRequestDTO {
    private String triggerId;
    private String triggerType;
    private String conditionExpression;
    private boolean enabled;
    private String triggerValue;
}

