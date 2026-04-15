package org.chamsoft.smarthome.gateway.automation.presentationlayer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerRuleRequestDTO {

    private String triggerId;
    private String triggerType;
    private String conditionExpression;
    private boolean enabled;
}
