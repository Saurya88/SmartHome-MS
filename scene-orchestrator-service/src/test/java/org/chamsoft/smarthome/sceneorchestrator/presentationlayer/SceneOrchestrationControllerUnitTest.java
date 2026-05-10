package org.chamsoft.smarthome.sceneorchestrator.presentationlayer;

import org.chamsoft.smarthome.sceneorchestrator.businesslogiclayer.SceneOrchestrationService;
import org.chamsoft.smarthome.sceneorchestrator.utilities.ResourceNotFoundException;
import org.chamsoft.smarthome.sceneorchestrator.utilities.SceneInvariantViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(SceneOrchestrationController.class)
class SceneOrchestrationControllerUnitTest {

    private WebTestClient webTestClient;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @MockBean
    private SceneOrchestrationService sceneOrchestrationService;

    @Test
    void getAllSceneOrchestrations_whenServiceReturnsData_returnsOk() {
        SceneOrchestrationResponseDTO dto = SceneOrchestrationResponseDTO.builder()
                .orchestrationId(UUID.randomUUID())
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(UUID.randomUUID())
                .name("Night Routine")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        when(sceneOrchestrationService.getAllSceneOrchestrations()).thenReturn(List.of(dto));

        webTestClient.get()
                .uri("/api/v1/scene-orchestrations")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Night Routine");
    }

    @Test
    void deleteSceneOrchestration_whenServiceSucceeds_returnsNoContent() {
        UUID id = UUID.randomUUID();
        doNothing().when(sceneOrchestrationService).deleteSceneOrchestration(id);

        webTestClient.delete()
                .uri("/api/v1/scene-orchestrations/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getSceneOrchestrationById_whenServiceThrowsNotFound_returnsNotFound() {
        UUID id = UUID.randomUUID();
        when(sceneOrchestrationService.getSceneOrchestrationById(id))
                .thenThrow(new ResourceNotFoundException("Scene orchestration not found"));

        webTestClient.get()
                .uri("/api/v1/scene-orchestrations/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Scene orchestration not found");
    }

    @Test
    void createSceneOrchestration_whenInvariantFails_returnsUnprocessableEntity() {
        when(sceneOrchestrationService.createSceneOrchestration(any()))
                .thenThrow(new SceneInvariantViolationException("Owner user must be ACTIVE."));

        webTestClient.post()
                .uri("/api/v1/scene-orchestrations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest())
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.status").isEqualTo(422)
                .jsonPath("$.message").isEqualTo("Owner user must be ACTIVE.");
    }

    private SceneOrchestrationRequestDTO validRequest() {
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
}
