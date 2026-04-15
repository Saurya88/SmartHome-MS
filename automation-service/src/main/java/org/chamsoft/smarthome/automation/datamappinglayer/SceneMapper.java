package org.chamsoft.smarthome.automation.datamappinglayer;

import org.chamsoft.smarthome.automation.domain.Scene;
import org.chamsoft.smarthome.automation.domain.TriggerRule;
import org.chamsoft.smarthome.automation.presentationlayer.SceneResponseDTO;
import org.chamsoft.smarthome.automation.presentationlayer.TriggerRuleResponseDTO;


public class SceneMapper {

    public static SceneResponseDTO toDTO(Scene scene) {

        SceneResponseDTO dto = new SceneResponseDTO();

        dto.setSceneId(scene.getSceneId());
        dto.setName(scene.getName());
        dto.setStatus(scene.getStatus());
        dto.setCreatedAt(scene.getCreatedAt());

        dto.setTriggerRules(
                scene.getTriggerRules().stream()
                        .map(SceneMapper::toTriggerRuleDTO)
                        .toList()
        );

        dto.setDeviceActions(
                scene.getDeviceActions().stream()
                        .map(action -> {
                            SceneResponseDTO.DeviceActionDTO dtoAction =
                                    new SceneResponseDTO.DeviceActionDTO();

                            dtoAction.setDeviceId(action.getDeviceId());
                            dtoAction.setActionType(action.getActionType());
                            dtoAction.setParameters(action.getParameters());


                            return dtoAction;
                        })
                        .toList()
        );

        return dto;
    }

    private static TriggerRuleResponseDTO toTriggerRuleDTO(TriggerRule triggerRule) {
        TriggerRuleResponseDTO dto = new TriggerRuleResponseDTO();
        dto.setTriggerId(triggerRule.getTriggerId());
        dto.setTriggerType(String.valueOf(triggerRule.getTriggerType()));
        dto.setConditionExpression(triggerRule.getConditionExpression());
        dto.setEnabled(triggerRule.isEnabled());
        dto.setValue(triggerRule.getTriggerValue());
        return dto;
    }
}