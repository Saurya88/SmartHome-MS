package org.chamsoft.smarthome.automation.presentationlayer;

import org.chamsoft.smarthome.automation.businesslogiclayer.SceneService;
import org.chamsoft.smarthome.automation.domain.Scene;
import org.chamsoft.smarthome.automation.utilities.InvalidSceneActivationException;
import org.chamsoft.smarthome.automation.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(SceneController.class)
class SceneControllerUnitTest {

    private WebTestClient webTestClient;

    @MockBean
    private SceneService sceneService;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Test
    void createScene_whenServiceReturnsScene_returnsCreated() {
        Scene scene = new Scene("Evening Mode");
        when(sceneService.createScene(anyString())).thenReturn(scene);

        webTestClient.post()
                .uri("/api/v1/scenes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Evening Mode"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("Location", ".*/api/v1/scenes/" + scene.getSceneId())
                .expectBody()
                .jsonPath("$.name").isEqualTo("Evening Mode");
    }

    @Test
    void getScene_whenServiceReturnsScene_returnsOk() {
        Scene scene = new Scene("Evening Mode");
        when(sceneService.getSceneOrThrow(scene.getSceneId())).thenReturn(scene);

        webTestClient.get()
                .uri("/api/v1/scenes/{id}", scene.getSceneId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sceneId").isEqualTo(scene.getSceneId().toString())
                .jsonPath("$.name").isEqualTo("Evening Mode");
    }

    @Test
    void getScene_whenServiceThrowsNotFound_returnsNotFound() {
        UUID sceneId = UUID.randomUUID();
        when(sceneService.getSceneOrThrow(sceneId)).thenThrow(new ResourceNotFoundException("Scene not found"));

        webTestClient.get()
                .uri("/api/v1/scenes/{id}", sceneId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Scene not found");
    }

    @Test
    void activateScene_whenSceneIncomplete_returnsConflict() {
        UUID sceneId = UUID.randomUUID();
        when(sceneService.activateScene(sceneId)).thenThrow(new InvalidSceneActivationException("Scene is incomplete"));

        webTestClient.put()
                .uri("/api/v1/scenes/{id}/activate", sceneId)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Scene is incomplete");
    }
}
