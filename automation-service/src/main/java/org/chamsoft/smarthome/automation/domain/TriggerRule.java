package org.chamsoft.smarthome.automation.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trigger_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerRule {

    @Id
    private String triggerId;

    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    private String conditionExpression;

    private boolean enabled;

    @Embedded
    private Condition condition;

    @Embedded
    private Schedule schedule;

    private String triggerValue;
}