package org.chamsoft.smarthome.gateway.presentationlayer;

import org.chamsoft.smarthome.gateway.automation.domainclientlayer.SceneService;
import org.chamsoft.smarthome.gateway.automation.domainclientlayer.TriggerRuleService;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.DeviceActionDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.DeviceActionRequestDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.SceneController;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.SceneRequestDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.SceneResponseDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.TriggerRuleController;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.TriggerRuleRequestDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.TriggerRuleResponseDTO;
import org.chamsoft.smarthome.gateway.device.domainclientlayer.DeviceService;
import org.chamsoft.smarthome.gateway.device.presentationlayer.DeviceController;
import org.chamsoft.smarthome.gateway.device.presentationlayer.DeviceRequestDTO;
import org.chamsoft.smarthome.gateway.device.presentationlayer.DeviceResponseDTO;
import org.chamsoft.smarthome.gateway.sceneorchestration.domainclientlayer.SceneOrchestrationService;
import org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer.SceneOrchestrationController;
import org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer.SceneOrchestrationRequestDTO;
import org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer.SceneOrchestrationResponseDTO;
import org.chamsoft.smarthome.gateway.userpermissions.domainclientlayer.UserService;
import org.chamsoft.smarthome.gateway.userpermissions.presentationlayer.UserController;
import org.chamsoft.smarthome.gateway.userpermissions.presentationlayer.UserRequestDTO;
import org.chamsoft.smarthome.gateway.userpermissions.presentationlayer.UserResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class GatewayControllerUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private SceneService sceneService;

    @Mock
    private TriggerRuleService triggerRuleService;

    @Mock
    private SceneOrchestrationService sceneOrchestrationService;

    private UserController userController;
    private DeviceController deviceController;
    private SceneController sceneController;
    private TriggerRuleController triggerRuleController;
    private SceneOrchestrationController sceneOrchestrationController;

    @BeforeEach
    void setUp() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        userController = new UserController(userService);
        deviceController = new DeviceController(deviceService);
        sceneController = new SceneController(sceneService);
        triggerRuleController = new TriggerRuleController(triggerRuleService);
        sceneOrchestrationController = new SceneOrchestrationController(sceneOrchestrationService);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void userControllerCrudMethods_returnExpectedResponses() {
        UUID userId = UUID.randomUUID();
        UserResponseDTO user = userResponse(userId);
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.createUser(any())).thenReturn(user);
        when(userService.updateUser(eq(userId), any())).thenReturn(user);
        doNothing().when(userService).deleteUser(userId);

        assertThat(userController.getAllUsers().getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(userController.getUserById(userId).getBody().getContent().getUsername()).isEqualTo("owner");
        assertThat(userController.createUser(new UserRequestDTO()).getStatusCodeValue()).isEqualTo(201);
        assertThat(userController.updateUser(userId, new UserRequestDTO()).getStatusCodeValue()).isEqualTo(200);
        assertThat(userController.deleteUser(userId).getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    void deviceControllerCrudMethods_returnExpectedResponses() {
        UUID deviceId = UUID.randomUUID();
        DeviceResponseDTO device = deviceResponse(deviceId);
        when(deviceService.getAllDevices()).thenReturn(List.of(device));
        when(deviceService.getDeviceById(deviceId)).thenReturn(device);
        when(deviceService.createDevice(any())).thenReturn(device);
        when(deviceService.updateDevice(eq(deviceId), any())).thenReturn(device);
        doNothing().when(deviceService).deleteDevice(deviceId);

        assertThat(deviceController.getAllDevices().getBody().getContent()).hasSize(1);
        assertThat(deviceController.getDeviceById(deviceId).getBody().getContent().getDeviceId()).isEqualTo(deviceId);
        assertThat(deviceController.createDevice(deviceRequest()).getStatusCodeValue()).isEqualTo(201);
        assertThat(deviceController.updateDevice(deviceId, deviceRequest()).getStatusCodeValue()).isEqualTo(200);
        assertThat(deviceController.deleteDevice(deviceId).getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    void sceneControllerMethods_returnExpectedResponses() {
        UUID sceneId = UUID.randomUUID();
        SceneResponseDTO scene = sceneResponse(sceneId);
        when(sceneService.getAllScenes()).thenReturn(List.of(scene));
        when(sceneService.getSceneById(sceneId)).thenReturn(scene);
        when(sceneService.createScene(any())).thenReturn(scene);
        when(sceneService.updateScene(eq(sceneId), any())).thenReturn(scene);
        when(sceneService.addDeviceActions(eq(sceneId), any())).thenReturn(scene);
        when(sceneService.replaceDeviceActions(eq(sceneId), any())).thenReturn(scene);
        when(sceneService.addTriggerRules(eq(sceneId), any())).thenReturn(scene);
        when(sceneService.replaceTriggerRules(eq(sceneId), any())).thenReturn(scene);
        when(sceneService.activateScene(sceneId)).thenReturn(scene);
        doNothing().when(sceneService).deleteScene(sceneId);

        assertThat(sceneController.getAllScenes().getBody().getContent()).hasSize(1);
        assertThat(sceneController.getSceneById(sceneId).getBody().getContent().getSceneId()).isEqualTo(sceneId);
        assertThat(sceneController.createScene(new SceneRequestDTO("Night Routine", "ACTIVE")).getStatusCodeValue()).isEqualTo(201);
        assertThat(sceneController.updateScene(sceneId, new SceneRequestDTO("Night Routine", "ACTIVE")).getStatusCodeValue()).isEqualTo(200);
        assertThat(sceneController.addDeviceActions(sceneId, List.of(deviceActionRequest())).getStatusCodeValue()).isEqualTo(200);
        assertThat(sceneController.replaceDeviceActions(sceneId, List.of(deviceActionRequest())).getStatusCodeValue()).isEqualTo(200);
        assertThat(sceneController.addTriggerRules(sceneId, List.of(triggerRuleRequest())).getStatusCodeValue()).isEqualTo(200);
        assertThat(sceneController.replaceTriggerRules(sceneId, List.of(triggerRuleRequest())).getStatusCodeValue()).isEqualTo(200);
        assertThat(sceneController.activateScene(sceneId).getBody().getContent().getStatus()).isEqualTo("ACTIVE");
        assertThat(sceneController.deleteScene(sceneId).getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    void triggerRuleControllerCrudMethods_returnExpectedResponses() {
        TriggerRuleResponseDTO triggerRule = triggerRuleResponse();
        when(triggerRuleService.getAllTriggerRules()).thenReturn(List.of(triggerRule));
        when(triggerRuleService.getTriggerRuleById(triggerRule.getTriggerId())).thenReturn(triggerRule);
        when(triggerRuleService.createTriggerRule(any())).thenReturn(triggerRule);
        when(triggerRuleService.updateTriggerRule(eq(triggerRule.getTriggerId()), any())).thenReturn(triggerRule);
        doNothing().when(triggerRuleService).deleteTriggerRule(triggerRule.getTriggerId());

        assertThat(triggerRuleController.getAllTriggerRules().getBody().getContent()).hasSize(1);
        assertThat(triggerRuleController.getTriggerRuleById(triggerRule.getTriggerId()).getBody().getContent().getTriggerId())
                .isEqualTo(triggerRule.getTriggerId());
        assertThat(triggerRuleController.createTriggerRule(triggerRuleRequest()).getStatusCodeValue()).isEqualTo(201);
        assertThat(triggerRuleController.updateTriggerRule(triggerRule.getTriggerId(), triggerRuleRequest()).getStatusCodeValue()).isEqualTo(200);
        assertThat(triggerRuleController.deleteTriggerRule(triggerRule.getTriggerId()).getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    void sceneOrchestrationControllerCrudMethods_returnExpectedResponses() {
        UUID orchestrationId = UUID.randomUUID();
        SceneOrchestrationResponseDTO responseDTO = orchestrationResponse(orchestrationId);
        when(sceneOrchestrationService.getAllSceneOrchestrations()).thenReturn(List.of(responseDTO));
        when(sceneOrchestrationService.getSceneOrchestrationById(orchestrationId)).thenReturn(responseDTO);
        when(sceneOrchestrationService.createSceneOrchestration(any())).thenReturn(responseDTO);
        when(sceneOrchestrationService.updateSceneOrchestration(eq(orchestrationId), any())).thenReturn(responseDTO);
        doNothing().when(sceneOrchestrationService).deleteSceneOrchestration(orchestrationId);

        assertThat(sceneOrchestrationController.getAllSceneOrchestrations().getBody().getContent()).hasSize(1);
        assertThat(sceneOrchestrationController.getSceneOrchestrationById(orchestrationId).getBody().getContent().getOrchestrationId())
                .isEqualTo(orchestrationId);
        assertThat(sceneOrchestrationController.createSceneOrchestration(orchestrationRequest()).getStatusCodeValue()).isEqualTo(201);
        assertThat(sceneOrchestrationController.updateSceneOrchestration(orchestrationId, orchestrationRequest()).getStatusCodeValue()).isEqualTo(200);
        assertThat(sceneOrchestrationController.deleteSceneOrchestration(orchestrationId).getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    void sceneController_whenDownstreamThrowsNotFound_propagatesException() {
        UUID sceneId = UUID.randomUUID();
        when(sceneService.getSceneById(sceneId)).thenThrow(HttpClientErrorException.create(
                NOT_FOUND, "Not Found", null, null, null
        ));

        assertThatThrownBy(() -> sceneController.getSceneById(sceneId))
                .isInstanceOf(HttpClientErrorException.class);
    }

    private UserResponseDTO userResponse(UUID userId) {
        return UserResponseDTO.builder()
                .userId(userId)
                .username("owner")
                .email("owner@smarthome.com")
                .role("OWNER")
                .accountStatus("ACTIVE")
                .permissions(List.of())
                .build();
    }

    private DeviceRequestDTO deviceRequest() {
        return DeviceRequestDTO.builder()
                .deviceName("Front Door Lock")
                .deviceType("LOCK")
                .status("ONLINE")
                .firmwareVersion("1.2.3")
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();
    }

    private DeviceResponseDTO deviceResponse(UUID deviceId) {
        return DeviceResponseDTO.builder()
                .deviceId(deviceId)
                .deviceName("Front Door Lock")
                .deviceType("LOCK")
                .status("ONLINE")
                .firmwareVersion("1.2.3")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();
    }

    private SceneResponseDTO sceneResponse(UUID sceneId) {
        return new SceneResponseDTO(
                sceneId,
                "Night Routine",
                "ACTIVE",
                LocalDateTime.now(),
                List.of(triggerRuleResponse()),
                List.of(DeviceActionDTO.builder()
                        .deviceId(UUID.randomUUID())
                        .actionType("LOCK")
                        .parameters("{}")
                        .build())
        );
    }

    private TriggerRuleRequestDTO triggerRuleRequest() {
        return TriggerRuleRequestDTO.builder()
                .triggerId("night-trigger")
                .triggerType("TIME_BASED")
                .conditionExpression("time == 22:00")
                .enabled(true)
                .triggerValue("22:00")
                .build();
    }

    private TriggerRuleResponseDTO triggerRuleResponse() {
        return TriggerRuleResponseDTO.builder()
                .triggerId("night-trigger")
                .triggerType("TIME_BASED")
                .conditionExpression("time == 22:00")
                .enabled(true)
                .value("22:00")
                .build();
    }

    private DeviceActionRequestDTO deviceActionRequest() {
        return DeviceActionRequestDTO.builder()
                .deviceId(UUID.randomUUID())
                .actionType("LOCK")
                .parameters("{}")
                .build();
    }

    private SceneOrchestrationRequestDTO orchestrationRequest() {
        return SceneOrchestrationRequestDTO.builder()
                .ownerUserId(UUID.randomUUID())
                .name("Night Routine")
                .status("ACTIVE")
                .deviceActions(List.of(SceneOrchestrationRequestDTO.DeviceActionRequestDTO.builder()
                        .deviceId(UUID.randomUUID())
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

    private SceneOrchestrationResponseDTO orchestrationResponse(UUID orchestrationId) {
        return SceneOrchestrationResponseDTO.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(UUID.randomUUID())
                .name("Night Routine")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of(SceneOrchestrationResponseDTO.DeviceActionResponseDTO.builder()
                        .deviceId(UUID.randomUUID())
                        .actionType("LOCK")
                        .parameters("{}")
                        .build()))
                .triggerRules(List.of(SceneOrchestrationResponseDTO.TriggerRuleResponseDTO.builder()
                        .triggerId("night-trigger")
                        .triggerType("TIME_BASED")
                        .conditionExpression("time == 22:00")
                        .enabled(true)
                        .triggerValue("22:00")
                        .build()))
                .build();
    }
}
