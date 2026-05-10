package org.chamsoft.smarthome.sceneorchestrator.datamappinglayer;

import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedDeviceAction;
import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedTriggerRule;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestrationStatus;
import org.chamsoft.smarthome.sceneorchestrator.presentationlayer.SceneOrchestrationRequestDTO;
import org.chamsoft.smarthome.sceneorchestrator.presentationlayer.SceneOrchestrationResponseDTO;

import java.util.List;
import java.util.UUID;

public final class SceneOrchestrationMapper {

    private SceneOrchestrationMapper() {
    }

    public static SceneOrchestration toEntity(SceneOrchestrationRequestDTO requestDTO,
                                              UUID automationSceneId,
                                              SceneOrchestrationStatus status) {
        return SceneOrchestration.builder()
                .ownerUserId(requestDTO.getOwnerUserId())
                .automationSceneId(automationSceneId)
                .name(requestDTO.getName())
                .status(status)
                .deviceActions(toManagedDeviceActions(requestDTO.getDeviceActions()))
                .triggerRules(toManagedTriggerRules(requestDTO.getTriggerRules()))
                .build();
    }

    public static void updateEntity(SceneOrchestration orchestration,
                                    SceneOrchestrationRequestDTO requestDTO,
                                    SceneOrchestrationStatus status) {
        orchestration.setOwnerUserId(requestDTO.getOwnerUserId());
        orchestration.setName(requestDTO.getName());
        orchestration.setStatus(status);
        orchestration.setDeviceActions(toManagedDeviceActions(requestDTO.getDeviceActions()));
        orchestration.setTriggerRules(toManagedTriggerRules(requestDTO.getTriggerRules()));
    }

    public static SceneOrchestrationResponseDTO toResponseDTO(SceneOrchestration orchestration) {
        return SceneOrchestrationResponseDTO.builder()
                .orchestrationId(orchestration.getOrchestrationId())
                .ownerUserId(orchestration.getOwnerUserId())
                .automationSceneId(orchestration.getAutomationSceneId())
                .name(orchestration.getName())
                .status(orchestration.getStatus().name())
                .createdAt(orchestration.getCreatedAt())
                .deviceActions(orchestration.getDeviceActions().stream()
                        .map(deviceAction -> SceneOrchestrationResponseDTO.DeviceActionResponseDTO.builder()
                                .deviceId(deviceAction.getDeviceId())
                                .actionType(deviceAction.getActionType())
                                .parameters(deviceAction.getParameters())
                                .build())
                        .toList())
                .triggerRules(orchestration.getTriggerRules().stream()
                        .map(triggerRule -> SceneOrchestrationResponseDTO.TriggerRuleResponseDTO.builder()
                                .triggerId(triggerRule.getTriggerId())
                                .triggerType(triggerRule.getTriggerType())
                                .conditionExpression(triggerRule.getConditionExpression())
                                .enabled(triggerRule.isEnabled())
                                .triggerValue(triggerRule.getTriggerValue())
                                .build())
                        .toList())
                .build();
    }

    private static List<ManagedDeviceAction> toManagedDeviceActions(List<SceneOrchestrationRequestDTO.DeviceActionRequestDTO> actions) {
        return actions.stream()
                .map(action -> ManagedDeviceAction.builder()
                        .deviceId(action.getDeviceId())
                        .actionType(action.getActionType())
                        .parameters(action.getParameters())
                        .build())
                .toList();
    }

    private static List<ManagedTriggerRule> toManagedTriggerRules(List<SceneOrchestrationRequestDTO.TriggerRuleRequestDTO> rules) {
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
