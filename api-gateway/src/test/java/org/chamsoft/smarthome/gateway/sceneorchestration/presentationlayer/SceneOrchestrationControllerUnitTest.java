package org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chamsoft.smarthome.gateway.sceneorchestration.domainclientlayer.SceneOrchestrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(SceneOrchestrationController.class)
class SceneOrchestrationControllerUnitTest {

    private WebTestClient webTestClient;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SceneOrchestrationService sceneOrchestrationService;

    @Test
    void getAllSceneOrchestrations_whenServiceReturnsData_returnsOk() throws Exception {
        UUID orchestrationId = UUID.randomUUID();
        SceneOrchestrationResponseDTO dto = SceneOrchestrationResponseDTO.builder()
                .orchestrationId(orchestrationId)
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
                .jsonPath("$._embedded.sceneOrchestrationResponseDTOList[0].name").isEqualTo("Night Routine")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void getSceneOrchestrationById_whenServiceReturnsData_returnsOk() throws Exception {
        UUID orchestrationId = UUID.randomUUID();
        SceneOrchestrationResponseDTO dto = SceneOrchestrationResponseDTO.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(UUID.randomUUID())
                .name("Night Routine")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        when(sceneOrchestrationService.getSceneOrchestrationById(orchestrationId)).thenReturn(dto);

        webTestClient.get()
                .uri("/api/v1/scene-orchestrations/{id}", orchestrationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orchestrationId").isEqualTo(orchestrationId.toString())
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void createSceneOrchestration_whenServiceSucceeds_returnsCreated() throws Exception {
        UUID orchestrationId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();

        SceneOrchestrationRequestDTO requestDTO = SceneOrchestrationRequestDTO.builder()
                .ownerUserId(ownerUserId)
                .name("Night Routine")
                .status("ACTIVE")
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        SceneOrchestrationResponseDTO dto = SceneOrchestrationResponseDTO.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(ownerUserId)
                .automationSceneId(UUID.randomUUID())
                .name("Night Routine")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        when(sceneOrchestrationService.createSceneOrchestration(any())).thenReturn(dto);

        webTestClient.post()
                .uri("/api/v1/scene-orchestrations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.orchestrationId").isEqualTo(orchestrationId.toString());
    }

    @Test
    void updateSceneOrchestration_whenServiceSucceeds_returnsOk() throws Exception {
        UUID orchestrationId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();

        SceneOrchestrationRequestDTO requestDTO = SceneOrchestrationRequestDTO.builder()
                .ownerUserId(ownerUserId)
                .name("Night Routine Updated")
                .status("INACTIVE")
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        SceneOrchestrationResponseDTO dto = SceneOrchestrationResponseDTO.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(ownerUserId)
                .automationSceneId(UUID.randomUUID())
                .name("Night Routine Updated")
                .status("INACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        when(sceneOrchestrationService.updateSceneOrchestration(eq(orchestrationId), any())).thenReturn(dto);

        webTestClient.put()
                .uri("/api/v1/scene-orchestrations/{id}", orchestrationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Night Routine Updated");
    }

    @Test
    void deleteSceneOrchestration_whenServiceSucceeds_returnsNoContent() throws Exception {
        UUID orchestrationId = UUID.randomUUID();

        doNothing().when(sceneOrchestrationService).deleteSceneOrchestration(orchestrationId);

        webTestClient.delete()
                .uri("/api/v1/scene-orchestrations/{id}", orchestrationId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void createSceneOrchestration_whenAggregatorRejectsInvariant_returnsUnprocessableEntity() throws Exception {
        SceneOrchestrationRequestDTO requestDTO = SceneOrchestrationRequestDTO.builder()
                .ownerUserId(UUID.randomUUID())
                .name("Night Routine")
                .status("ACTIVE")
                .deviceActions(List.of())
                .triggerRules(List.of())
                .build();

        when(sceneOrchestrationService.createSceneOrchestration(any())).thenThrow(HttpClientErrorException.create(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Unprocessable Entity",
                null,
                "{\"message\":\"Owner user must be ACTIVE.\"}".getBytes(),
                null
        ));

        webTestClient.post()
                .uri("/api/v1/scene-orchestrations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Owner user must be ACTIVE.");
    }
}
