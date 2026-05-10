package org.chamsoft.smarthome.sceneorchestrator.presentationlayer;

import org.chamsoft.smarthome.sceneorchestrator.dataccesslayer.SceneOrchestrationRepository;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.AutomationSceneResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.DeviceClientResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.DeviceServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.UserClientResponseDTO;
import org.chamsoft.smarthome.sceneorchestrator.domainclientlayer.UserServiceClient;
import org.chamsoft.smarthome.sceneorchestrator.testutils.EmbeddedMongoSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.data.mongodb.database=scene_orchestrator_controller_test"
})
class SceneOrchestrationControllerIntegrationTest {

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:" + EmbeddedMongoSupport.port() + "/scene_orchestrator_controller_test");
    }

    private WebTestClient webTestClient;

    @Autowired
    private WebApplicationContext webApplicationContext;

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
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Test
    void createSceneOrchestration_whenDeviceOffline_returnsUnprocessableEntity() {
        SceneOrchestrationRequestDTO requestDTO = validRequest();

        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(activeOwner(requestDTO));
        DeviceClientResponseDTO device = onlineDevice(requestDTO);
        device.setStatus("OFFLINE");
        when(deviceServiceClient.getDeviceById(requestDTO.getDeviceActions().getFirst().getDeviceId())).thenReturn(device);

        webTestClient.post()
                .uri("/api/v1/scene-orchestrations")
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Device " + requestDTO.getDeviceActions().getFirst().getDeviceId() + " must be ONLINE.");

        verifyNoInteractions(automationServiceClient);
    }

    @Test
    void getSceneOrchestrationById_whenMissing_returnsNotFound() {
        webTestClient.get()
                .uri("/api/v1/scene-orchestrations/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void createSceneOrchestration_whenValid_returnsCreated() {
        SceneOrchestrationRequestDTO requestDTO = validRequest();

        when(userServiceClient.getUserById(requestDTO.getOwnerUserId())).thenReturn(activeOwner(requestDTO));
        when(deviceServiceClient.getDeviceById(requestDTO.getDeviceActions().getFirst().getDeviceId())).thenReturn(onlineDevice(requestDTO));

        AutomationSceneResponseDTO createdScene = new AutomationSceneResponseDTO();
        createdScene.setSceneId(UUID.randomUUID());
        createdScene.setName(requestDTO.getName());
        createdScene.setStatus("INACTIVE");
        when(automationServiceClient.createScene(any())).thenReturn(createdScene);

        webTestClient.post()
                .uri("/api/v1/scene-orchestrations")
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Night Routine")
                .jsonPath("$.automationSceneId").isEqualTo(createdScene.getSceneId().toString());
    }

    @Test
    void getAllSceneOrchestrations_whenEmpty_returnsOkArray() {
        webTestClient.get()
                .uri("/api/v1/scene-orchestrations")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("[]");
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
}
