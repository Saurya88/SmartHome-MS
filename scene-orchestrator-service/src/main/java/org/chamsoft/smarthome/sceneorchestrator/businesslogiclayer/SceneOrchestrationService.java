package org.chamsoft.smarthome.sceneorchestrator.businesslogiclayer;

import org.chamsoft.smarthome.sceneorchestrator.dataccesslayer.SceneOrchestrationRepository;
import org.chamsoft.smarthome.sceneorchestrator.datamappinglayer.SceneOrchestrationMapper;
import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedDeviceAction;
import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedTriggerRule;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationDeviceActionRequestDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationSceneRequestDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationSceneResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationTriggerRuleRequestDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.DeviceClientResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.DeviceServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.UserClientResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.UserServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.presentationlayer.SceneOrchestrationRequestDTO;
import org.chamsoft.smarthome.sceneorchestrator.presentationlayer.SceneOrchestrationResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.utilities.ResourceNotFoundException;
import org.chamsoft.smarthome.sceneorchestrator.utilities.SceneInvariantViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SceneOrchestrationService {

    private static final Set<String> ALLOWED_ACCESS_LEVELS = Set.of("CONTROL", "FULL_ACCESS");

    private final SceneOrchestrationRepository repository;
    private final AutomationServiceClient automationServiceClient;
    private final DeviceServiceClient deviceServiceClient;
    private final UserServiceClient userServiceClient;

    public SceneOrchestrationService(SceneOrchestrationRepository repository,
                                     AutomationServiceClient automationServiceClient,
                                     DeviceServiceClient deviceServiceClient,
                                     UserServiceClient userServiceClient) {
        this.repository = repository;
        this.automationServiceClient = automationServiceClient;
        this.deviceServiceClient = deviceServiceClient;
        this.userServiceClient = userServiceClient;
    }

    public List<SceneOrchestrationResponseDTO> getAllSceneOrchestrations() {
        return repository.findAll().stream().map(SceneOrchestrationMapper::toResponseDTO).toList();
    }

    public SceneOrchestrationResponseDTO getSceneOrchestrationById(UUID orchestrationId) {
        return SceneOrchestrationMapper.toResponseDTO(getOrThrow(orchestrationId));
    }

    public SceneOrchestrationResponseDTO createSceneOrchestration(SceneOrchestrationRequestDTO requestDTO) {
        validateInvariant(requestDTO);

        AutomationSceneResponseDTO createdScene = automationServiceClient.createScene(AutomationSceneRequestDTO.builder()
                .name(requestDTO.getName())
                .status("INACTIVE")
                .build());

        UUID sceneId = createdScene.getSceneId();
        automationServiceClient.addDeviceActions(sceneId, requestDTO.getDeviceActions().stream()
                .map(action -> AutomationDeviceActionRequestDTO.builder()
                        .deviceId(action.getDeviceId())
                        .actionType(action.getActionType())
                        .parameters(action.getParameters())
                        .build())
                .toList());

        automationServiceClient.addTriggerRules(sceneId, requestDTO.getTriggerRules().stream()
                .map(rule -> AutomationTriggerRuleRequestDTO.builder()
                        .triggerId(rule.getTriggerId())
                        .triggerType(rule.getTriggerType())
                        .conditionExpression(rule.getConditionExpression())
                        .enabled(rule.isEnabled())
                        .triggerValue(rule.getTriggerValue())
                        .build())
                .toList());

        if ("ACTIVE".equalsIgnoreCase(requestDTO.getStatus())) {
            automationServiceClient.activateScene(sceneId);
        }

        SceneOrchestration entity = SceneOrchestrationMapper.toNewEntity(
                requestDTO,
                UUID.randomUUID(),
                sceneId
        );

        return SceneOrchestrationMapper.toResponseDTO(repository.save(entity));
    }

    public SceneOrchestrationResponseDTO updateSceneOrchestration(UUID orchestrationId, SceneOrchestrationRequestDTO requestDTO) {
        validateInvariant(requestDTO);

        SceneOrchestration existing = getOrThrow(orchestrationId);

        automationServiceClient.updateScene(existing.getAutomationSceneId(), AutomationSceneRequestDTO.builder()
                .name(requestDTO.getName())
                .status("INACTIVE")
                .build());

        automationServiceClient.replaceDeviceActions(existing.getAutomationSceneId(), requestDTO.getDeviceActions().stream()
                .map(action -> AutomationDeviceActionRequestDTO.builder()
                        .deviceId(action.getDeviceId())
                        .actionType(action.getActionType())
                        .parameters(action.getParameters())
                        .build())
                .toList());

        automationServiceClient.replaceTriggerRules(existing.getAutomationSceneId(), requestDTO.getTriggerRules().stream()
                .map(rule -> AutomationTriggerRuleRequestDTO.builder()
                        .triggerId(rule.getTriggerId())
                        .triggerType(rule.getTriggerType())
                        .conditionExpression(rule.getConditionExpression())
                        .enabled(rule.isEnabled())
                        .triggerValue(rule.getTriggerValue())
                        .build())
                .toList());

        if ("ACTIVE".equalsIgnoreCase(requestDTO.getStatus())) {
            automationServiceClient.activateScene(existing.getAutomationSceneId());
        }

        SceneOrchestrationMapper.updateEntity(existing, requestDTO);
        return SceneOrchestrationMapper.toResponseDTO(repository.save(existing));
    }

    public void deleteSceneOrchestration(UUID orchestrationId) {
        SceneOrchestration existing = getOrThrow(orchestrationId);
        repository.deleteById(orchestrationId);
        automationServiceClient.deleteScene(existing.getAutomationSceneId());
    }

    private SceneOrchestration getOrThrow(UUID orchestrationId) {
        return repository.findById(orchestrationId).orElseThrow(() ->
                new ResourceNotFoundException("Scene orchestration not found: " + orchestrationId));
    }

    private void validateInvariant(SceneOrchestrationRequestDTO requestDTO) {
        if (requestDTO.getDeviceActions() == null || requestDTO.getDeviceActions().isEmpty()) {
            throw new SceneInvariantViolationException("A scene orchestration must contain at least one device action.");
        }
        if (requestDTO.getTriggerRules() == null || requestDTO.getTriggerRules().isEmpty()) {
            throw new SceneInvariantViolationException("A scene orchestration must contain at least one trigger rule.");
        }

        UserClientResponseDTO owner = userServiceClient.getUserById(requestDTO.getOwnerUserId());
        if (owner == null || !"ACTIVE".equalsIgnoreCase(owner.getAccountStatus())) {
            throw new SceneInvariantViolationException("Owner user must be ACTIVE.");
        }

        for (ManagedDeviceAction action : requestDTO.getDeviceActions().stream()
                .map(a -> ManagedDeviceAction.builder()
                        .deviceId(a.getDeviceId())
                        .actionType(a.getActionType())
                        .parameters(a.getParameters())
                        .build())
                .toList()) {

            validateDevice(owner, action.getDeviceId());
        }

        for (ManagedTriggerRule ignored : requestDTO.getTriggerRules().stream()
                .map(r -> ManagedTriggerRule.builder()
                        .triggerId(r.getTriggerId())
                        .triggerType(r.getTriggerType())
                        .conditionExpression(r.getConditionExpression())
                        .enabled(r.isEnabled())
                        .triggerValue(r.getTriggerValue())
                        .build())
                .toList()) {
            // Request DTO validation handles required fields.
        }
    }

    private void validateDevice(UserClientResponseDTO owner, UUID deviceId) {
        boolean hasPermission = owner.getPermissions() != null && owner.getPermissions().stream()
                .anyMatch(p -> deviceId.equals(p.getDeviceId()) && p.getAccessLevel() != null
                        && ALLOWED_ACCESS_LEVELS.contains(p.getAccessLevel().toUpperCase()));

        if (!hasPermission) {
            throw new SceneInvariantViolationException("Owner user must have CONTROL or FULL_ACCESS to device " + deviceId + ".");
        }

        DeviceClientResponseDTO device = deviceServiceClient.getDeviceById(deviceId);
        if (device == null || !"ONLINE".equalsIgnoreCase(device.getStatus())) {
            throw new SceneInvariantViolationException("Device " + deviceId + " must be ONLINE.");
        }
        if (device.getFirmwareVersion() == null || device.getFirmwareVersion().isBlank()) {
            throw new SceneInvariantViolationException("Device " + deviceId + " must have a valid firmwareVersion.");
        }
    }
}

