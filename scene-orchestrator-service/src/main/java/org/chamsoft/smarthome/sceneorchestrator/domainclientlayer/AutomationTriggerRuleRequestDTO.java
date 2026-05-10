package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutomationTriggerRuleRequestDTO {
    private String triggerId;
    private String triggerType;
    private String conditionExpression;
    private boolean enabled;
    private String triggerValue;
}
