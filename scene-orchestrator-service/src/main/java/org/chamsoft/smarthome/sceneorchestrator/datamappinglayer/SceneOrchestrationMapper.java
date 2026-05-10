package org.chamsoft.smarthome.sceneorchestrator.datamappinglayer;

import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedDeviceAction;
import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedTriggerRule;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.chamsoft.smarthome.sceneorchestrator.presentationlayer.SceneOrchestrationRequestDTO;
import org.chamsoft.smarthome.sceneorchestrator.presentationlayer.SceneOrchestrationResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SceneOrchestrationMapper {

    public static SceneOrchestration toNewEntity(SceneOrchestrationRequestDTO requestDTO, UUID orchestrationId, UUID automationSceneId) {
        return SceneOrchestration.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(requestDTO.getOwnerUserId())
                .automationSceneId(automationSceneId)
                .name(requestDTO.getName())
                .status(requestDTO.getStatus())
                .createdAt(LocalDateTime.now())
                .deviceActions(toDeviceActions(requestDTO.getDeviceActions()))
                .triggerRules(toTriggerRules(requestDTO.getTriggerRules()))
                .build();
    }

    public static void updateEntity(SceneOrchestration entity, SceneOrchestrationRequestDTO requestDTO) {
        entity.setOwnerUserId(requestDTO.getOwnerUserId());
        entity.setName(requestDTO.getName());
        entity.setStatus(requestDTO.getStatus());
        entity.setDeviceActions(toDeviceActions(requestDTO.getDeviceActions()));
        entity.setTriggerRules(toTriggerRules(requestDTO.getTriggerRules()));
    }

    public static SceneOrchestrationResponseDTO toResponseDTO(SceneOrchestration entity) {
        return SceneOrchestrationResponseDTO.builder()
                .orchestrationId(entity.getOrchestrationId())
                .ownerUserId(entity.getOwnerUserId())
                .automationSceneId(entity.getAutomationSceneId())
                .name(entity.getName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .deviceActions(entity.getDeviceActions().stream().map(action ->
                        SceneOrchestrationResponseDTO.DeviceActionResponseDTO.builder()
                                .deviceId(action.getDeviceId())
                                .actionType(action.getActionType())
                                .parameters(action.getParameters())
                                .build()
                ).toList())
                .triggerRules(entity.getTriggerRules().stream().map(rule ->
                        SceneOrchestrationResponseDTO.TriggerRuleResponseDTO.builder()
                                .triggerId(rule.getTriggerId())
                                .triggerType(rule.getTriggerType())
                                .conditionExpression(rule.getConditionExpression())
                                .enabled(rule.isEnabled())
                                .triggerValue(rule.getTriggerValue())
                                .build()
                ).toList())
                .build();
    }

    private static List<ManagedDeviceAction> toDeviceActions(List<SceneOrchestrationRequestDTO.DeviceActionRequestDTO> actions) {
        return actions.stream()
                .map(action -> ManagedDeviceAction.builder()
                        .deviceId(action.getDeviceId())
                        .actionType(action.getActionType())
                        .parameters(action.getParameters())
                        .build())
                .toList();
    }

    private static List<ManagedTriggerRule> toTriggerRules(List<SceneOrchestrationRequestDTO.TriggerRuleRequestDTO> rules) {
        return rules.stream()
                .map(rule -> ManagedTriggerRule.builder()
                        .triggerId(rule.getTriggerId())
                        .triggerType(rule.getTriggerType())
                        .conditionExpression(rule.getConditionExpression())
                        .enabled(rule.isEnabled())
                        .triggerValue(rule.getTriggerValue())
                        .build())
                .toList();
    }
}

