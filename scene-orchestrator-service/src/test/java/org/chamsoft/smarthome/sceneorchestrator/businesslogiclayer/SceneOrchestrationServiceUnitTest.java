package org.chamsoft.smarthome.sceneorchestrator.businesslogiclayer;

import org.chamsoft.smarthome.sceneorchestrator.dataccesslayer.SceneOrchestrationRepository;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationSceneResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.DeviceClientResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.DeviceServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.UserClientResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.UserServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.presentationlayer.SceneOrchestrationRequestDTO;
import org.chamsoft.smarthome.sceneorchestrator.utilities.ResourceNotFoundException;
import org.chamsoft.smarthome.sceneorchestrator.utilities.SceneInvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SceneOrchestrationServiceUnitTest {

    private final SceneOrchestrationRepository repository = mock(SceneOrchestrationRepository.class);
    private final AutomationServiceClient automationServiceClient = mock(AutomationServiceClient.class);
    private final DeviceServiceClient deviceServiceClient = mock(DeviceServiceClient.class);
    private final UserServiceClient userServiceClient = mock(UserServiceClient.class);

    private final SceneOrchestrationService service = new SceneOrchestrationService(
            repository,
            automationServiceClient,
            deviceServiceClient,
            userServiceClient
    );

    @Test
    void createSceneOrchestration_whenOwnerInactive_throwsInvariantViolation() {
        SceneOrchestrationRequestDTO request = validRequest();
        UserClientResponseDTO owner = new UserClientResponseDTO();
        owner.setUserId(request.getOwnerUserId());
        owner.setAccountStatus("SUSPENDED");
        owner.setPermissions(List.of());

        when(userServiceClient.getUserById(request.getOwnerUserId())).thenReturn(owner);

        assertThatThrownBy(() -> service.createSceneOrchestration(request))
                .isInstanceOf(SceneInvariantViolationException.class)
                .hasMessageContaining("Owner user must be ACTIVE");
    }

    @Test
    void deleteSceneOrchestration_whenMissing_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteSceneOrchestration(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSceneOrchestration_whenValidOrchestration_persistsAggregate() {
        SceneOrchestrationRequestDTO request = validRequest();

        when(userServiceClient.getUserById(request.getOwnerUserId())).thenReturn(activeOwnerWithPermission(request));
        when(deviceServiceClient.getDeviceById(request.getDeviceActions().getFirst().getDeviceId())).thenReturn(onlineDevice(request));

        AutomationSceneResponseDTO scene = new AutomationSceneResponseDTO();
        scene.setSceneId(UUID.randomUUID());
        scene.setName(request.getName());
        scene.setStatus("INACTIVE");
        when(automationServiceClient.createScene(any())).thenReturn(scene);

        when(repository.save(any(SceneOrchestration.class))).thenAnswer(inv -> inv.getArgument(0, SceneOrchestration.class));

        var created = service.createSceneOrchestration(request);

        assertThat(created.getAutomationSceneId()).isEqualTo(scene.getSceneId());
        verify(repository).save(any(SceneOrchestration.class));
    }

    @Test
    void createSceneOrchestration_whenMissingDeviceActions_throwsInvariantViolation() {
        SceneOrchestrationRequestDTO request = SceneOrchestrationRequestDTO.builder()
                .ownerUserId(UUID.randomUUID())
                .name("Bad Routine")
                .status("ACTIVE")
                .deviceActions(List.of())
                .triggerRules(List.of(SceneOrchestrationRequestDTO.TriggerRuleRequestDTO.builder()
                        .triggerId("rule-1")
                        .triggerType("TIME_BASED")
                        .conditionExpression("time == 22:00")
                        .enabled(true)
                        .triggerValue("22:00")
                        .build()))
                .build();

        assertThatThrownBy(() -> service.createSceneOrchestration(request))
                .isInstanceOf(SceneInvariantViolationException.class)
                .hasMessageContaining("at least one device action");

        verifyNoInteractions(automationServiceClient);
        verifyNoInteractions(deviceServiceClient);
        verifyNoInteractions(userServiceClient);
    }

    @Test
    void createSceneOrchestration_whenOwnerLacksPermission_throwsInvariantViolation() {
        SceneOrchestrationRequestDTO request = validRequest();

        UserClientResponseDTO owner = new UserClientResponseDTO();
        owner.setUserId(request.getOwnerUserId());
        owner.setAccountStatus("ACTIVE");
        owner.setPermissions(List.of());

        when(userServiceClient.getUserById(request.getOwnerUserId())).thenReturn(owner);

        assertThatThrownBy(() -> service.createSceneOrchestration(request))
                .isInstanceOf(SceneInvariantViolationException.class)
                .hasMessageContaining("CONTROL or FULL_ACCESS");

        verifyNoInteractions(automationServiceClient);
    }

    @Test
    void createSceneOrchestration_whenFirmwareMissing_throwsInvariantViolation() {
        SceneOrchestrationRequestDTO request = validRequest();

        when(userServiceClient.getUserById(request.getOwnerUserId())).thenReturn(activeOwnerWithPermission(request));

        DeviceClientResponseDTO device = onlineDevice(request);
        device.setFirmwareVersion(" ");
        when(deviceServiceClient.getDeviceById(request.getDeviceActions().getFirst().getDeviceId())).thenReturn(device);

        assertThatThrownBy(() -> service.createSceneOrchestration(request))
                .isInstanceOf(SceneInvariantViolationException.class)
                .hasMessageContaining("valid firmwareVersion");

        verifyNoInteractions(automationServiceClient);
    }

    @Test
    void updateSceneOrchestration_whenMissing_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        SceneOrchestrationRequestDTO request = validRequest();
        when(userServiceClient.getUserById(request.getOwnerUserId())).thenReturn(activeOwnerWithPermission(request));
        when(deviceServiceClient.getDeviceById(request.getDeviceActions().getFirst().getDeviceId())).thenReturn(onlineDevice(request));

        assertThatThrownBy(() -> service.updateSceneOrchestration(id, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(automationServiceClient);
    }

    @Test
    void updateSceneOrchestration_whenStatusActive_activatesScene() {
        SceneOrchestrationRequestDTO request = validRequest();

        SceneOrchestration existing = SceneOrchestration.builder()
                .orchestrationId(UUID.randomUUID())
                .ownerUserId(request.getOwnerUserId())
                .automationSceneId(UUID.randomUUID())
                .name("Old")
                .status("INACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        when(userServiceClient.getUserById(request.getOwnerUserId())).thenReturn(activeOwnerWithPermission(request));
        when(deviceServiceClient.getDeviceById(request.getDeviceActions().getFirst().getDeviceId())).thenReturn(onlineDevice(request));
        when(repository.findById(existing.getOrchestrationId())).thenReturn(Optional.of(existing));
        when(repository.save(any(SceneOrchestration.class))).thenAnswer(inv -> inv.getArgument(0, SceneOrchestration.class));

        var updated = service.updateSceneOrchestration(existing.getOrchestrationId(), request);

        assertThat(updated.getOrchestrationId()).isEqualTo(existing.getOrchestrationId());
        verify(automationServiceClient).activateScene(existing.getAutomationSceneId());
        verify(repository).save(any(SceneOrchestration.class));
    }

    @Test
    void deleteSceneOrchestration_whenExists_deletesAndCallsAutomationDelete() {
        UUID orchestrationId = UUID.randomUUID();
        UUID automationSceneId = UUID.randomUUID();

        SceneOrchestration existing = SceneOrchestration.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(automationSceneId)
                .name("Night Routine")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        when(repository.findById(orchestrationId)).thenReturn(Optional.of(existing));

        service.deleteSceneOrchestration(orchestrationId);

        verify(repository).deleteById(orchestrationId);
        verify(automationServiceClient).deleteScene(automationSceneId);
    }

    private SceneOrchestrationRequestDTO validRequest() {
        UUID ownerUserId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        return SceneOrchestrationRequestDTO.builder()
                .ownerUserId(ownerUserId)
                .name("Night Routine")
                .status("ACTIVE")
                .deviceActions(List.of(SceneOrchestrationRequestDTO.DeviceActionRequestDTO.builder()
                        .deviceId(deviceId)
                        .actionType("LOCK")
                        .parameters("{}")
                        .build()))
                .triggerRules(List.of(SceneOrchestrationRequestDTO.TriggerRuleRequestDTO.builder()
                        .triggerId("night-trigger")
                        .triggerType("TIME_BASED")
                        .conditionExpression("time == 22:00")
                        .enabled(true)
                        .triggerValue("22:00")
                        .build()))
                .build();
    }

    private UserClientResponseDTO activeOwnerWithPermission(SceneOrchestrationRequestDTO requestDTO) {
        UserClientResponseDTO.PermissionClientDTO permission = new UserClientResponseDTO.PermissionClientDTO();
        permission.setDeviceId(requestDTO.getDeviceActions().getFirst().getDeviceId());
        permission.setAccessLevel("CONTROL");

        UserClientResponseDTO owner = new UserClientResponseDTO();
        owner.setUserId(requestDTO.getOwnerUserId());
        owner.setAccountStatus("ACTIVE");
        owner.setPermissions(List.of(permission));
        return owner;
    }

    private DeviceClientResponseDTO onlineDevice(SceneOrchestrationRequestDTO requestDTO) {
        DeviceClientResponseDTO device = new DeviceClientResponseDTO();
        device.setDeviceId(requestDTO.getDeviceActions().getFirst().getDeviceId());
        device.setDeviceName("Front Door Lock");
        device.setStatus("ONLINE");
        device.setFirmwareVersion("1.2.3");
        return device;
    }
}
