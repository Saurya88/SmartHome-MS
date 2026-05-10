package org.chamsoft.smarthome.sceneorchestrator.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagedTriggerRule {

    private String triggerId;

    private String triggerType;

    private String conditionExpression;

    private boolean enabled;

    private String triggerValue;
}
