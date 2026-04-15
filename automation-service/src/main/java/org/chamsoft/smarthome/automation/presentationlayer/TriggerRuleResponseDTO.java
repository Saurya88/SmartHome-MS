package org.chamsoft.smarthome.automation.presentationlayer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerRuleResponseDTO {

    private String triggerId;
    private String triggerType;
    private String conditionExpression;
    private boolean enabled;
    private String value;
}
