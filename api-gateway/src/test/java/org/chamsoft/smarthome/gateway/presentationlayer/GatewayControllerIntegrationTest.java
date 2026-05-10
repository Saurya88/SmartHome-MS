package org.chamsoft.smarthome.gateway.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chamsoft.smarthome.gateway.automation.domainclientlayer.SceneService;
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
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayControllerIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private DeviceService deviceService;

    @MockBean
    private SceneService sceneService;

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

        webTestClient.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.userResponseDTOList[0].username").isEqualTo("owner")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void createDevice_whenRequestIsValid_returnsCreatedDevice() throws Exception {
        UUID deviceId = UUID.randomUUID();
        DeviceRequestDTO requestDTO = DeviceRequestDTO.builder()
                .deviceName("Front Door Lock")
                .deviceType("LOCK")
                .firmwareVersion("1.2.3")
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();

        DeviceResponseDTO responseDTO = DeviceResponseDTO.builder()
                .deviceId(deviceId)
                .deviceName("Front Door Lock")
                .deviceType("LOCK")
                .firmwareVersion("1.2.3")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();

        when(deviceService.createDevice(any())).thenReturn(responseDTO);

        webTestClient.post()
                .uri("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.deviceName").isEqualTo("Front Door Lock")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void createSceneOrchestration_whenRequestIsValid_returnsCreatedAggregate() throws Exception {
        UUID orchestrationId = UUID.randomUUID();
        UUID automationSceneId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        SceneOrchestrationRequestDTO requestDTO = SceneOrchestrationRequestDTO.builder()
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

        webTestClient.post()
                .uri("/api/v1/scene-orchestrations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Night Routine")
                .jsonPath("$._links.self.href").exists();
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

        webTestClient.get()
                .uri("/api/v1/scenes/{id}", sceneId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Scene not found");
    }

    @Test
    void createSceneOrchestration_whenAggregatorReturnsInvariantError_translatesError() throws Exception {
        UUID ownerUserId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        SceneOrchestrationRequestDTO requestDTO = SceneOrchestrationRequestDTO.builder()
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

        when(sceneOrchestrationService.createSceneOrchestration(any())).thenThrow(HttpClientErrorException.create(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Unprocessable Entity",
                null,
                "{\"message\":\"Device must be ONLINE\"}".getBytes(),
                null
        ));

        webTestClient.post()
                .uri("/api/v1/scene-orchestrations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Device must be ONLINE");
    }
}
