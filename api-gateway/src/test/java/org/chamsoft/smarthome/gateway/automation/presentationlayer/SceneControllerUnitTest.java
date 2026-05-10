package org.chamsoft.smarthome.gateway.automation.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chamsoft.smarthome.gateway.automation.domainclientlayer.SceneService;
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

@WebMvcTest(SceneController.class)
class SceneControllerUnitTest {

    private WebTestClient webTestClient;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SceneService sceneService;

    @Test
    void getAllScenes_whenServiceReturnsData_returnsOk() throws Exception {
        SceneResponseDTO dto = new SceneResponseDTO(
                UUID.randomUUID(),
                "Night Routine",
                "ACTIVE",
                LocalDateTime.now(),
                List.of(),
                List.of()
        );

        when(sceneService.getAllScenes()).thenReturn(List.of(dto));

        webTestClient.get()
                .uri("/api/v1/scenes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.sceneResponseDTOList[0].name").isEqualTo("Night Routine")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void getSceneById_whenServiceReturnsData_returnsOk() throws Exception {
        UUID sceneId = UUID.randomUUID();
        SceneResponseDTO dto = new SceneResponseDTO(
                sceneId,
                "Night Routine",
                "ACTIVE",
                LocalDateTime.now(),
                List.of(),
                List.of()
        );

        when(sceneService.getSceneById(sceneId)).thenReturn(dto);

        webTestClient.get()
                .uri("/api/v1/scenes/{id}", sceneId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sceneId").isEqualTo(sceneId.toString())
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void createScene_whenServiceSucceeds_returnsCreated() throws Exception {
        UUID sceneId = UUID.randomUUID();
        SceneRequestDTO requestDTO = new SceneRequestDTO("Night Routine", "INACTIVE");

        SceneResponseDTO dto = new SceneResponseDTO(
                sceneId,
                "Night Routine",
                "INACTIVE",
                LocalDateTime.now(),
                List.of(),
                List.of()
        );

        when(sceneService.createScene(any())).thenReturn(dto);

        webTestClient.post()
                .uri("/api/v1/scenes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.sceneId").isEqualTo(sceneId.toString());
    }

    @Test
    void updateScene_whenServiceSucceeds_returnsOk() throws Exception {
        UUID sceneId = UUID.randomUUID();
        SceneRequestDTO requestDTO = new SceneRequestDTO("Updated", "ACTIVE");

        SceneResponseDTO dto = new SceneResponseDTO(
                sceneId,
                "Updated",
                "ACTIVE",
                LocalDateTime.now(),
                List.of(),
                List.of()
        );

        when(sceneService.updateScene(eq(sceneId), any())).thenReturn(dto);

        webTestClient.put()
                .uri("/api/v1/scenes/{id}", sceneId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Updated");
    }

    @Test
    void deleteScene_whenServiceSucceeds_returnsNoContent() throws Exception {
        UUID sceneId = UUID.randomUUID();
        doNothing().when(sceneService).deleteScene(sceneId);

        webTestClient.delete()
                .uri("/api/v1/scenes/{id}", sceneId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getSceneById_whenDownstreamReturnsNotFound_returnsNotFound() throws Exception {
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
}
