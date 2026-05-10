package org.chamsoft.smarthome.gateway.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chamsoft.smarthome.gateway.automation.domainclientlayer.SceneService;
import org.chamsoft.smarthome.gateway.automation.domainclientlayer.TriggerRuleService;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.DeviceActionDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.SceneResponseDTO;
import org.chamsoft.smarthome.gateway.device.domainclientlayer.DeviceService;
import org.chamsoft.smarthome.gateway.device.presentationlayer.DeviceRequestDTO;
import org.chamsoft.smarthome.gateway.device.presentationlayer.DeviceResponseDTO;
import org.chamsoft.smarthome.gateway.sceneorchestration.domainclientlayer.SceneOrchestrationService;
import org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer.SceneOrchestrationRequestDTO;
import org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer.SceneOrchestrationResponseDTO;
import org.chamsoft.smarthome.gateway.userpermissions.domainclientlayer.UserService;
import org.chamsoft.smarthome.gateway.userpermissions.presentationlayer.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private DeviceService deviceService;

    @MockBean
    private SceneService sceneService;

    @MockBean
    private TriggerRuleService triggerRuleService;

    @MockBean
    private SceneOrchestrationService sceneOrchestrationService;

    @Test
    void getAllUsers_whenServiceReturnsUsers_returnsHateoasCollection() throws Exception {
        UserResponseDTO user = UserResponseDTO.builder()
                .userId(UUID.randomUUID())
                .username("owner")
                .email("owner@smarthome.com")
                .role("OWNER")
                .accountStatus("ACTIVE")
                .permissions(List.of())
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userResponseDTOList[0].username").value("owner"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void createDevice_whenRequestIsValid_returnsCreatedDevice() throws Exception {
        UUID deviceId = UUID.randomUUID();
        DeviceRequestDTO requestDTO = DeviceRequestDTO.builder()
                .deviceName("Front Door Lock")
                .deviceType("LOCK")
                .status("ONLINE")
                .firmwareVersion("1.2.3")
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();

        DeviceResponseDTO responseDTO = DeviceResponseDTO.builder()
                .deviceId(deviceId)
                .deviceName("Front Door Lock")
                .deviceType("LOCK")
                .status("ONLINE")
                .firmwareVersion("1.2.3")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();

        when(deviceService.createDevice(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceName").value("Front Door Lock"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void activateScene_whenSceneExists_returnsUpdatedScene() throws Exception {
        UUID sceneId = UUID.randomUUID();
        SceneResponseDTO responseDTO = new SceneResponseDTO(
                sceneId,
                "Night Routine",
                "ACTIVE",
                LocalDateTime.now(),
                List.of(),
                List.of(DeviceActionDTO.builder()
                        .deviceId(UUID.randomUUID())
                        .actionType("LOCK")
                        .parameters("{}")
                        .build())
        );

        when(sceneService.activateScene(sceneId)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/scenes/{id}/activate", sceneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createSceneOrchestration_whenRequestIsValid_returnsCreatedAggregate() throws Exception {
        UUID orchestrationId = UUID.randomUUID();
        UUID automationSceneId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        SceneOrchestrationRequestDTO requestDTO = validOrchestrationRequest(ownerUserId, deviceId);
        SceneOrchestrationResponseDTO responseDTO = SceneOrchestrationResponseDTO.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(ownerUserId)
                .automationSceneId(automationSceneId)
                .name("Night Routine")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of(SceneOrchestrationResponseDTO.DeviceActionResponseDTO.builder()
                        .deviceId(deviceId)
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

        when(sceneOrchestrationService.createSceneOrchestration(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/scene-orchestrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Night Routine"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void getSceneById_whenDownstreamReturnsNotFound_translatesError() throws Exception {
        UUID sceneId = UUID.randomUUID();

        when(sceneService.getSceneById(sceneId)).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "{\"message\":\"Scene not found\"}".getBytes(),
                null
        ));

        mockMvc.perform(get("/api/v1/scenes/{id}", sceneId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Scene not found"));
    }

    @Test
    void createSceneOrchestration_whenBodyIsInvalid_returnsBadRequest() throws Exception {
        SceneOrchestrationRequestDTO requestDTO = SceneOrchestrationRequestDTO.builder()
                .ownerUserId(UUID.randomUUID())
                .name("Invalid")
                .status("ACTIVE")
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        mockMvc.perform(post("/api/v1/scene-orchestrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    private SceneOrchestrationRequestDTO validOrchestrationRequest(UUID ownerUserId, UUID deviceId) {
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
}
