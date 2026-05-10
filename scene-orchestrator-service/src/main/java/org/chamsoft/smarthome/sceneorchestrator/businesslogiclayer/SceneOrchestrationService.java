package org.chamsoft.smarthome.sceneorchestrator.businesslogiclayer;

import org.chamsoft.smarthome.sceneorchestrator.dataccesslayer.SceneOrchestrationRepository;
import org.chamsoft.smarthome.sceneorchestrator.datamappinglayer.SceneOrchestrationMapper;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestrationStatus;
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
import org.chamsoft.smarthome.sceneorchestrator.utilities.InvalidInputException;
import org.chamsoft.smarthome.sceneorchestrator.utilities.ResourceNotFoundException;
import org.chamsoft.smarthome.sceneorchestrator.utilities.SceneInvariantViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SceneOrchestrationService {

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

    @Transactional(readOnly = true)
    public List<SceneOrchestrationResponseDTO> getAllSceneOrchestrations() {
        return repository.findAll().stream()
                .map(SceneOrchestrationMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SceneOrchestrationResponseDTO getSceneOrchestrationById(UUID orchestrationId) {
        return SceneOrchestrationMapper.toResponseDTO(getOrchestrationOrThrow(orchestrationId));
    }

    @Transactional
    public SceneOrchestrationResponseDTO createSceneOrchestration(SceneOrchestrationRequestDTO requestDTO) {
        validateInvariant(requestDTO);
        UserClientResponseDTO owner = getOwnerOrThrow(requestDTO.getOwnerUserId());
        validateOwnerAndDevices(owner, requestDTO.getDeviceActions());

        AutomationSceneResponseDTO sceneResponse = automationServiceClient.createScene(
                new AutomationSceneRequestDTO(requestDTO.getName(), null)
        );

        automationServiceClient.replaceDeviceActions(sceneResponse.getSceneId(), toAutomationDeviceActions(requestDTO));
        automationServiceClient.replaceTriggerRules(sceneResponse.getSceneId(), toAutomationTriggerRules(requestDTO));

        SceneOrchestrationStatus finalStatus = applyRequestedStatus(sceneResponse.getSceneId(), requestDTO);

        SceneOrchestration orchestration = SceneOrchestrationMapper.toEntity(
                requestDTO,
                sceneResponse.getSceneId(),
                finalStatus
        );

        return SceneOrchestrationMapper.toResponseDTO(repository.save(orchestration));
    }

    @Transactional
    public SceneOrchestrationResponseDTO updateSceneOrchestration(UUID orchestrationId,
                                                                  SceneOrchestrationRequestDTO requestDTO) {
        SceneOrchestration existing = getOrchestrationOrThrow(orchestrationId);

        validateInvariant(requestDTO);
        UserClientResponseDTO owner = getOwnerOrThrow(requestDTO.getOwnerUserId());
        validateOwnerAndDevices(owner, requestDTO.getDeviceActions());

        automationServiceClient.updateScene(
                existing.getAutomationSceneId(),
                new AutomationSceneRequestDTO(requestDTO.getName(), null)
        );
        automationServiceClient.replaceDeviceActions(existing.getAutomationSceneId(), toAutomationDeviceActions(requestDTO));
        automationServiceClient.replaceTriggerRules(existing.getAutomationSceneId(), toAutomationTriggerRules(requestDTO));

        SceneOrchestrationStatus finalStatus = applyRequestedStatus(existing.getAutomationSceneId(), requestDTO);
        SceneOrchestrationMapper.updateEntity(existing, requestDTO, finalStatus);

        return SceneOrchestrationMapper.toResponseDTO(repository.save(existing));
    }

    @Transactional
    public void deleteSceneOrchestration(UUID orchestrationId) {
        SceneOrchestration orchestration = getOrchestrationOrThrow(orchestrationId);

        try {
            automationServiceClient.deleteScene(orchestration.getAutomationSceneId());
        } catch (HttpClientErrorException.NotFound ignored) {
            // Keep the aggregator store consistent even if the low-level scene was already removed.
        }

        repository.delete(orchestration);
    }

    private SceneOrchestration getOrchestrationOrThrow(UUID orchestrationId) {
        return repository.findById(orchestrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Scene orchestration not found with ID: " + orchestrationId));
    }

    private UserClientResponseDTO getOwnerOrThrow(UUID ownerUserId) {
        try {
            return userServiceClient.getUserById(ownerUserId);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new InvalidInputException("Owner user not found with ID: " + ownerUserId);
        }
    }

    private DeviceClientResponseDTO getDeviceOrThrow(UUID deviceId) {
        try {
            return deviceServiceClient.getDeviceById(deviceId);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new InvalidInputException("Device not found with ID: " + deviceId);
        }
    }

    private void validateInvariant(SceneOrchestrationRequestDTO requestDTO) {
        if (requestDTO.getDeviceActions() == null || requestDTO.getDeviceActions().isEmpty()) {
            throw new SceneInvariantViolationException("A scene orchestration must contain at least one device action.");
        }

        if (requestDTO.getTriggerRules() == null || requestDTO.getTriggerRules().isEmpty()) {
            throw new SceneInvariantViolationException("A scene orchestration must contain at least one trigger rule.");
        }
    }

    private void validateOwnerAndDevices(UserClientResponseDTO owner,
                                         List<SceneOrchestrationRequestDTO.DeviceActionRequestDTO> actions) {
        if (owner == null) {
            throw new InvalidInputException("Owner user is required.");
        }

        if (!"ACTIVE".equalsIgnoreCase(owner.getAccountStatus())) {
            throw new SceneInvariantViolationException("Owner user account must be ACTIVE.");
        }

        for (SceneOrchestrationRequestDTO.DeviceActionRequestDTO action : actions) {
            DeviceClientResponseDTO device = getDeviceOrThrow(action.getDeviceId());

            if (!"ONLINE".equalsIgnoreCase(device.getStatus())) {
                throw new SceneInvariantViolationException("Device " + action.getDeviceId() + " must be ONLINE.");
            }

            if (device.getFirmwareVersion() == null || device.getFirmwareVersion().isBlank()) {
                throw new SceneInvariantViolationException("Device " + action.getDeviceId() + " must have a valid firmware version.");
            }

            boolean hasPermission = owner.getPermissions() != null && owner.getPermissions().stream()
                    .anyMatch(permission -> action.getDeviceId().equals(permission.getDeviceId())
                            && hasControlAccess(permission.getAccessLevel()));

            if (!hasPermission) {
                throw new SceneInvariantViolationException(
                        "Owner user must have CONTROL or FULL_ACCESS permission for device " + action.getDeviceId() + "."
                );
            }
        }
    }

    private boolean hasControlAccess(String accessLevel) {
        if (accessLevel == null) {
            return false;
        }

        String normalized = accessLevel.toUpperCase(Locale.ROOT);
        return "CONTROL".equals(normalized) || "FULL_ACCESS".equals(normalized);
    }

    private SceneOrchestrationStatus applyRequestedStatus(UUID sceneId, SceneOrchestrationRequestDTO requestDTO) {
        SceneOrchestrationStatus requestedStatus = normalizeStatus(requestDTO.getStatus());

        if (requestedStatus == SceneOrchestrationStatus.ACTIVE) {
            automationServiceClient.activateScene(sceneId);
            return SceneOrchestrationStatus.ACTIVE;
        }

        if (requestedStatus == SceneOrchestrationStatus.SCHEDULED) {
            automationServiceClient.updateScene(sceneId, new AutomationSceneRequestDTO(requestDTO.getName(), "SCHEDULED"));
            return SceneOrchestrationStatus.SCHEDULED;
        }

        if (requestedStatus == SceneOrchestrationStatus.INACTIVE) {
            automationServiceClient.updateScene(sceneId, new AutomationSceneRequestDTO(requestDTO.getName(), "INACTIVE"));
        }

        return requestedStatus;
    }

    private SceneOrchestrationStatus normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return SceneOrchestrationStatus.INACTIVE;
        }

        try {
            return SceneOrchestrationStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Unsupported status: " + status);
        }
    }

    private List<AutomationDeviceActionRequestDTO> toAutomationDeviceActions(SceneOrchestrationRequestDTO requestDTO) {
        return requestDTO.getDeviceActions().stream()
                .map(action -> new AutomationDeviceActionRequestDTO(
                        action.getDeviceId(),
                        action.getActionType(),
                        action.getParameters()
                ))
                .toList();
    }

    private List<AutomationTriggerRuleRequestDTO> toAutomationTriggerRules(SceneOrchestrationRequestDTO requestDTO) {
        return requestDTO.getTriggerRules().stream()
                .map(rule -> new AutomationTriggerRuleRequestDTO(
                        rule.getTriggerId(),
                        rule.getTriggerType(),
                        rule.getConditionExpression(),
                        rule.isEnabled(),
                        rule.getTriggerValue()
                ))
                .toList();
    }
}
