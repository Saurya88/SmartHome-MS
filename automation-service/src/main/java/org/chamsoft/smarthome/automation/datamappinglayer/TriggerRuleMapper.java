package org.chamsoft.smarthome.automation.datamappinglayer;

import org.chamsoft.smarthome.automation.domain.TriggerRule;
import org.chamsoft.smarthome.automation.domain.TriggerType;
import org.chamsoft.smarthome.automation.presentationlayer.TriggerRuleRequestDTO;
import org.chamsoft.smarthome.automation.presentationlayer.TriggerRuleResponseDTO;

public class TriggerRuleMapper {

    public static TriggerRule toEntity(TriggerRuleRequestDTO dto) {
        return TriggerRule.builder()
                .triggerId(dto.getTriggerId())
                .triggerType(Enum.valueOf(TriggerType.class, dto.getTriggerType()))
                .conditionExpression(dto.getConditionExpression())
                .enabled(dto.isEnabled())
                .triggerValue(dto.getTriggerValue())
                .build();
    }

    public static TriggerRuleResponseDTO toDTO(TriggerRule entity) {
        return TriggerRuleResponseDTO.builder()
                .triggerId(entity.getTriggerId())
                .triggerType(entity.getTriggerType().name())
                .conditionExpression(entity.getConditionExpression())
                .enabled(entity.isEnabled())
                .value(entity.getTriggerValue())
                .build();
    }
}
