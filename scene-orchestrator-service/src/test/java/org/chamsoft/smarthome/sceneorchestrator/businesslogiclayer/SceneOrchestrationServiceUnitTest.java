package org.chamsoft.smarthome.sceneorchestrator.businesslogiclayer;

import org.chamsoft.smarthome.sceneorchestrator.dataccesslayer.SceneOrchestrationRepository;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestrationStatus;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationSceneResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.DeviceClientResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.DeviceServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.UserClientResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.UserServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.presentationlayer.SceneOrchestrationRequestDTO;
import org.chamsoft.smarthome.sceneorchestrator.presentationlayer.SceneOrchestrationResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.utilities.ResourceNotFoundException;
import org.chamsoft.smarthome.sceneorchestrator.utilities.SceneInvariantViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class SceneOrchestrationServiceUnitTest {

    @Mock
    private SceneOrchestrationRepository repository;

    @Mock
    private AutomationServiceClient automationServiceClient;

    @Mock
    private DeviceServiceClient deviceServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private SceneOrchestrationService service;

    @Test
    void createSceneOrchestration_whenRequestIsValidAndActive_returnsActiveAggregate() {
        SceneOrchestrationRequestDTO requestDTO = validRequest("ACTIVE");
        UUID sceneId = UUID.randomUUID();

        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(activeOwner(requestDTO));
        when(deviceServiceClient.getDeviceById(requestDTO.getDeviceActions().getFirst().getDeviceId()))
                .thenReturn(onlineDevice(requestDTO));
        when(automationServiceClient.createScene(any())).thenReturn(sceneResponse(sceneId, "INACTIVE"));
        when(automationServiceClient.activateScene(sceneId)).thenReturn(sceneResponse(sceneId, "ACTIVE"));
        when(repository.save(any(SceneOrchestration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SceneOrchestrationResponseDTO responseDTO = service.createSceneOrchestration(requestDTO);

        assertThat(responseDTO.getStatus()).isEqualTo("ACTIVE");
        assertThat(responseDTO.getAutomationSceneId()).isEqualTo(sceneId);

        ArgumentCaptor<SceneOrchestration> captor = ArgumentCaptor.forClass(SceneOrchestration.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SceneOrchestrationStatus.ACTIVE);
        verify(automationServiceClient).replaceDeviceActions(eq(sceneId), any());
        verify(automationServiceClient).replaceTriggerRules(eq(sceneId), any());
    }

    @Test
    void updateSceneOrchestration_whenScheduled_updatesAggregateAndDownstreamScene() {
        SceneOrchestrationRequestDTO requestDTO = validRequest("SCHEDULED");
        UUID orchestrationId = UUID.randomUUID();
        UUID sceneId = UUID.randomUUID();
        SceneOrchestration existing = SceneOrchestration.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(requestDTO.getOwnerUserId())
                .automationSceneId(sceneId)
                .name("Existing Scene")
                .status(SceneOrchestrationStatus.INACTIVE)
                .build();

        when(repository.findById(orchestrationId)).thenReturn(Optional.of(existing));
        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(activeOwner(requestDTO));
        when(deviceServiceClient.getDeviceById(requestDTO.getDeviceActions().getFirst().getDeviceId()))
                .thenReturn(onlineDevice(requestDTO));
        when(automationServiceClient.updateScene(eq(sceneId), any())).thenReturn(sceneResponse(sceneId, "SCHEDULED"));
        when(repository.save(existing)).thenReturn(existing);

        SceneOrchestrationResponseDTO responseDTO = service.updateSceneOrchestration(orchestrationId, requestDTO);

        assertThat(responseDTO.getStatus()).isEqualTo("SCHEDULED");
        verify(automationServiceClient).replaceDeviceActions(eq(sceneId), any());
        verify(automationServiceClient).replaceTriggerRules(eq(sceneId), any());
    }

    @Test
    void createSceneOrchestration_whenUserLacksPermission_throwsInvariantViolation() {
        SceneOrchestrationRequestDTO requestDTO = validRequest("INACTIVE");
        UserClientResponseDTO owner = new UserClientResponseDTO();
        owner.setUserId(requestDTO.getOwnerUserId());
        owner.setAccountStatus("ACTIVE");
        owner.setPermissions(List.of());

        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(owner);
        when(deviceServiceClient.getDeviceById(requestDTO.getDeviceActions().getFirst().getDeviceId()))
                .thenReturn(onlineDevice(requestDTO));

        assertThatThrownBy(() -> service.createSceneOrchestration(requestDTO))
                .isInstanceOf(SceneInvariantViolationException.class)
                .hasMessageContaining("CONTROL or FULL_ACCESS");
    }

    @Test
    void getAllSceneOrchestrations_whenRepositoryHasData_returnsMappedResponses() {
        SceneOrchestration orchestration = SceneOrchestration.builder()
                .orchestrationId(UUID.randomUUID())
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(UUID.randomUUID())
                .name("Night Routine")
                .status(SceneOrchestrationStatus.INACTIVE)
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        when(repository.findAll()).thenReturn(List.of(orchestration));

        assertThat(service.getAllSceneOrchestrations()).hasSize(1);
    }

    @Test
    void createSceneOrchestration_whenOwnerIsNull_throwsInvalidInput() {
        SceneOrchestrationRequestDTO requestDTO = validRequest("INACTIVE");
        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(null);

        assertThatThrownBy(() -> service.createSceneOrchestration(requestDTO))
                .hasMessage("Owner user is required.");
    }

    @Test
    void createSceneOrchestration_whenOwnerIsSuspended_throwsInvariantViolation() {
        SceneOrchestrationRequestDTO requestDTO = validRequest("INACTIVE");
        UserClientResponseDTO owner = activeOwner(requestDTO);
        owner.setAccountStatus("SUSPENDED");

        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(owner);

        assertThatThrownBy(() -> service.createSceneOrchestration(requestDTO))
                .isInstanceOf(SceneInvariantViolationException.class)
                .hasMessage("Owner user account must be ACTIVE.");
    }

    @Test
    void createSceneOrchestration_whenDeviceFirmwareIsBlank_throwsInvariantViolation() {
        SceneOrchestrationRequestDTO requestDTO = validRequest("INACTIVE");
        DeviceClientResponseDTO device = onlineDevice(requestDTO);
        device.setFirmwareVersion(" ");

        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(activeOwner(requestDTO));
        when(deviceServiceClient.getDeviceById(requestDTO.getDeviceActions().getFirst().getDeviceId())).thenReturn(device);

        assertThatThrownBy(() -> service.createSceneOrchestration(requestDTO))
                .isInstanceOf(SceneInvariantViolationException.class)
                .hasMessageContaining("must have a valid firmware version");
    }

    @Test
    void createSceneOrchestration_whenDeviceDoesNotExist_throwsInvalidInput() {
        SceneOrchestrationRequestDTO requestDTO = validRequest("INACTIVE");

        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(activeOwner(requestDTO));
        when(deviceServiceClient.getDeviceById(requestDTO.getDeviceActions().getFirst().getDeviceId()))
                .thenThrow(HttpClientErrorException.create(NOT_FOUND, "Not Found", null, null, null));

        assertThatThrownBy(() -> service.createSceneOrchestration(requestDTO))
                .hasMessageContaining("Device not found with ID");
    }

    @Test
    void createSceneOrchestration_whenStatusIsBlank_returnsInactiveAggregate() {
        SceneOrchestrationRequestDTO requestDTO = validRequest("");
        UUID sceneId = UUID.randomUUID();

        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(activeOwner(requestDTO));
        when(deviceServiceClient.getDeviceById(requestDTO.getDeviceActions().getFirst().getDeviceId()))
                .thenReturn(onlineDevice(requestDTO));
        when(automationServiceClient.createScene(any())).thenReturn(sceneResponse(sceneId, "INACTIVE"));
        when(automationServiceClient.updateScene(eq(sceneId), any())).thenReturn(sceneResponse(sceneId, "INACTIVE"));
        when(repository.save(any(SceneOrchestration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SceneOrchestrationResponseDTO responseDTO = service.createSceneOrchestration(requestDTO);

        assertThat(responseDTO.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void createSceneOrchestration_whenNoDeviceActions_throwsInvariantViolation() {
        SceneOrchestrationRequestDTO requestDTO = validRequest("INACTIVE");
        requestDTO.setDeviceActions(List.of());

        assertThatThrownBy(() -> service.createSceneOrchestration(requestDTO))
                .isInstanceOf(SceneInvariantViolationException.class)
                .hasMessage("A scene orchestration must contain at least one device action.");
    }

    @Test
    void createSceneOrchestration_whenNoTriggerRules_throwsInvariantViolation() {
        SceneOrchestrationRequestDTO requestDTO = validRequest("INACTIVE");
        requestDTO.setTriggerRules(List.of());

        assertThatThrownBy(() -> service.createSceneOrchestration(requestDTO))
                .isInstanceOf(SceneInvariantViolationException.class)
                .hasMessage("A scene orchestration must contain at least one trigger rule.");
    }

    @Test
    void getSceneOrchestrationById_whenMissing_throwsNotFound() {
        UUID orchestrationId = UUID.randomUUID();
        when(repository.findById(orchestrationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSceneOrchestrationById(orchestrationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(orchestrationId.toString());
    }

    @Test
    void deleteSceneOrchestration_whenDownstreamSceneIsAlreadyMissing_stillDeletesAggregate() {
        UUID orchestrationId = UUID.randomUUID();
        UUID sceneId = UUID.randomUUID();
        SceneOrchestration existing = SceneOrchestration.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(sceneId)
                .name("Existing Scene")
                .status(SceneOrchestrationStatus.INACTIVE)
                .build();

        when(repository.findById(orchestrationId)).thenReturn(Optional.of(existing));
        doThrow(HttpClientErrorException.create(NOT_FOUND, "Not Found", null, null, null))
                .when(automationServiceClient).deleteScene(sceneId);
        doNothing().when(repository).delete(existing);

        service.deleteSceneOrchestration(orchestrationId);

        verify(repository).delete(existing);
    }

    private SceneOrchestrationRequestDTO validRequest(String status) {
        UUID ownerUserId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        return SceneOrchestrationRequestDTO.builder()
                .ownerUserId(ownerUserId)
                .name("Movie Night")
                .status(status)
                .deviceActions(List.of(SceneOrchestrationRequestDTO.DeviceActionRequestDTO.builder()
                        .deviceId(deviceId)
                        .actionType("TURN_ON")
                        .parameters("{}")
                        .build()))
                .triggerRules(List.of(SceneOrchestrationRequestDTO.TriggerRuleRequestDTO.builder()
                        .triggerId("movie-trigger")
                        .triggerType("MANUAL")
                        .conditionExpression("manual == true")
                        .enabled(true)
                        .triggerValue("MANUAL")
                        .build()))
                .build();
    }

    private UserClientResponseDTO activeOwner(SceneOrchestrationRequestDTO requestDTO) {
        UserClientResponseDTO.PermissionClientDTO permission = new UserClientResponseDTO.PermissionClientDTO();
        permission.setDeviceId(requestDTO.getDeviceActions().getFirst().getDeviceId());
        permission.setAccessLevel("FULL_ACCESS");

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

    private AutomationSceneResponseDTO sceneResponse(UUID sceneId, String status) {
        AutomationSceneResponseDTO responseDTO = new AutomationSceneResponseDTO();
        responseDTO.setSceneId(sceneId);
        responseDTO.setName("Movie Night");
        responseDTO.setStatus(status);
        return responseDTO;
    }
}
