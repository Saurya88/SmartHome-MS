package org.chamsoft.smarthome.sceneorchestrator.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chamsoft.smarthome.sceneorchestrator.dataccesslayer.SceneOrchestrationRepository;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationSceneResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.DeviceClientResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.DeviceServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.UserClientResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc
class SceneOrchestrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SceneOrchestrationRepository repository;

    @MockBean
    private AutomationServiceClient automationServiceClient;

    @MockBean
    private DeviceServiceClient deviceServiceClient;

    @MockBean
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createSceneOrchestration_whenRequestIsValid_returnsCreatedAggregate() throws Exception {
        SceneOrchestrationRequestDTO requestDTO = validRequest("ACTIVE");
        UUID sceneId = UUID.randomUUID();

        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(activeOwner(requestDTO));
        when(deviceServiceClient.getDeviceById(requestDTO.getDeviceActions().getFirst().getDeviceId()))
                .thenReturn(onlineDevice(requestDTO));
        when(automationServiceClient.createScene(any())).thenReturn(sceneResponse(sceneId, "INACTIVE"));
        when(automationServiceClient.activateScene(sceneId)).thenReturn(sceneResponse(sceneId, "ACTIVE"));

        mockMvc.perform(post("/api/v1/scene-orchestrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.automationSceneId").value(sceneId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.deviceActions.length()").value(1))
                .andExpect(jsonPath("$.triggerRules.length()").value(1));
    }

    @Test
    void getSceneOrchestrationById_whenAggregateDoesNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/scene-orchestrations/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createSceneOrchestration_whenDeviceIsOffline_returnsConflict() throws Exception {
        SceneOrchestrationRequestDTO requestDTO = validRequest("INACTIVE");

        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(activeOwner(requestDTO));
        DeviceClientResponseDTO device = onlineDevice(requestDTO);
        device.setStatus("OFFLINE");
        when(deviceServiceClient.getDeviceById(requestDTO.getDeviceActions().getFirst().getDeviceId())).thenReturn(device);

        mockMvc.perform(post("/api/v1/scene-orchestrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Device " + requestDTO.getDeviceActions().getFirst().getDeviceId() + " must be ONLINE."));

        verifyNoInteractions(automationServiceClient);
    }

    @Test
    void createSceneOrchestration_whenBodyIsInvalid_returnsBadRequest() throws Exception {
        SceneOrchestrationRequestDTO requestDTO = validRequest("INACTIVE");
        requestDTO.setDeviceActions(List.of());

        mockMvc.perform(post("/api/v1/scene-orchestrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("deviceActions: deviceActions must contain at least one entry"));
    }

    private SceneOrchestrationRequestDTO validRequest(String status) {
        UUID ownerUserId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        return SceneOrchestrationRequestDTO.builder()
                .ownerUserId(ownerUserId)
                .name("Night Routine")
                .status(status)
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

    private UserClientResponseDTO activeOwner(SceneOrchestrationRequestDTO requestDTO) {
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

    private AutomationSceneResponseDTO sceneResponse(UUID sceneId, String status) {
        AutomationSceneResponseDTO responseDTO = new AutomationSceneResponseDTO();
        responseDTO.setSceneId(sceneId);
        responseDTO.setName("Night Routine");
        responseDTO.setStatus(status);
        return responseDTO;
    }
}
