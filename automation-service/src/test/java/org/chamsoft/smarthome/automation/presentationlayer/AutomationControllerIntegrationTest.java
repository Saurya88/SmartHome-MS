package org.chamsoft.smarthome.automation.presentationlayer;

import org.chamsoft.smarthome.automation.dataccesslayer.SceneRepository;
import org.chamsoft.smarthome.automation.dataccesslayer.TriggerRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.sql.init.mode=never"
)
@AutoConfigureWebTestClient
class AutomationControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private TriggerRuleRepository triggerRuleRepository;

    @BeforeEach
    void setUp() {
        sceneRepository.deleteAll();
        triggerRuleRepository.deleteAll();
    }

    @Test
    void createAndGetScene_whenRequestIsValid_returnsCreatedScene() {
        SceneResponseDTO created = createScene("Evening Mode");

        webTestClient.get()
                .uri("/api/v1/scenes/{id}", created.getSceneId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(SceneResponseDTO.class)
                .value(response -> {
                    assertThat(response.getName()).isEqualTo("Evening Mode");
                    assertThat(response.getStatus().name()).isEqualTo("INACTIVE");
                });
    }

    @Test
    void getAllScenes_whenScenesExist_returnsCollection() {
        createScene("Scene One");
        createScene("Scene Two");

        webTestClient.get()
                .uri("/api/v1/scenes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SceneResponseDTO.class)
                .value(response -> assertThat(response).hasSize(2));
    }

    @Test
    void updateScene_whenSceneExists_returnsUpdatedScene() {
        SceneResponseDTO created = createScene("Morning Mode");

        webTestClient.put()
                .uri("/api/v1/scenes/{id}", created.getSceneId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Updated Morning Mode", "status", "SCHEDULED"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SceneResponseDTO.class)
                .value(response -> {
                    assertThat(response.getSceneId()).isEqualTo(created.getSceneId());
                    assertThat(response.getName()).isEqualTo("Updated Morning Mode");
                    assertThat(response.getStatus().name()).isEqualTo("SCHEDULED");
                });
    }

    @Test
    void deleteScene_whenSceneExists_returnsNoContent() {
        SceneResponseDTO created = createScene("Delete Scene");

        webTestClient.delete()
                .uri("/api/v1/scenes/{id}", created.getSceneId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(sceneRepository.findById(created.getSceneId())).isEmpty();
    }

    @Test
    void getSceneById_whenSceneDoesNotExist_returnsNotFound() {
        webTestClient.get()
                .uri("/api/v1/scenes/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Scene not found");
    }

    @Test
    void activateScene_whenSceneHasNoActionsOrRules_returnsConflict() {
        SceneResponseDTO created = createScene("Incomplete Scene");

        webTestClient.put()
                .uri("/api/v1/scenes/{id}/activate", created.getSceneId())
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.message").isEqualTo("Scene must contain at least one TriggerRule and one DeviceAction to activate.");
    }

    @Test
    void activateScene_whenSceneHasActionAndTriggerRule_returnsActiveScene() {
        SceneResponseDTO created = createScene("Complete Scene");

        webTestClient.post()
                .uri("/api/v1/scenes/{id}/device-actions", created.getSceneId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(Map.of(
                        "deviceId", UUID.randomUUID(),
                        "actionType", "TURN_ON",
                        "parameters", "{}"
                )))
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri("/api/v1/scenes/{id}/trigger-rules", created.getSceneId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(triggerRuleRequest("scene-trigger", "MANUAL", "manual == true", true)))
                .exchange()
                .expectStatus().isOk();

        webTestClient.put()
                .uri("/api/v1/scenes/{id}/activate", created.getSceneId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(SceneResponseDTO.class)
                .value(response -> {
                    assertThat(response.getStatus().name()).isEqualTo("ACTIVE");
                    assertThat(response.getDeviceActions()).hasSize(1);
                    assertThat(response.getTriggerRules()).hasSize(1);
                });
    }

    @Test
    void replaceSceneActionsAndRules_whenSceneExists_returnsOnlyReplacementCollections() {
        SceneResponseDTO created = createScene("Replace Collections Scene");

        webTestClient.post()
                .uri("/api/v1/scenes/{id}/device-actions", created.getSceneId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(Map.of(
                        "deviceId", UUID.randomUUID(),
                        "actionType", "TURN_ON",
                        "parameters", "{}"
                )))
                .exchange()
                .expectStatus().isOk();

        webTestClient.put()
                .uri("/api/v1/scenes/{id}/device-actions", created.getSceneId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(Map.of(
                        "deviceId", UUID.randomUUID(),
                        "actionType", "TURN_OFF",
                        "parameters", "{}"
                )))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SceneResponseDTO.class)
                .value(response -> {
                    assertThat(response.getDeviceActions()).hasSize(1);
                    assertThat(response.getDeviceActions().getFirst().getActionType()).isEqualTo("TURN_OFF");
                });

        webTestClient.post()
                .uri("/api/v1/scenes/{id}/trigger-rules", created.getSceneId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(triggerRuleRequest("original-trigger", "MANUAL", "manual == true", true)))
                .exchange()
                .expectStatus().isOk();

        webTestClient.put()
                .uri("/api/v1/scenes/{id}/trigger-rules", created.getSceneId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(triggerRuleRequest("replacement-trigger", "TIME_BASED", "time == 23:00", true)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(SceneResponseDTO.class)
                .value(response -> {
                    assertThat(response.getTriggerRules()).hasSize(1);
                    assertThat(response.getTriggerRules().getFirst().getTriggerId()).isEqualTo("replacement-trigger");
                });
    }

    @Test
    void createAndGetTriggerRule_whenRequestIsValid_returnsCreatedTriggerRule() {
        TriggerRuleResponseDTO created = createTriggerRule("trigger-evening");

        webTestClient.get()
                .uri("/api/v1/trigger-rules/{id}", created.getTriggerId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TriggerRuleResponseDTO.class)
                .value(response -> {
                    assertThat(response.getTriggerId()).isEqualTo("trigger-evening");
                    assertThat(response.getTriggerType()).isEqualTo("MANUAL");
                    assertThat(response.isEnabled()).isTrue();
                });
    }

    @Test
    void getAllTriggerRules_whenTriggerRulesExist_returnsCollection() {
        createTriggerRule("trigger-one");
        createTriggerRule("trigger-two");

        webTestClient.get()
                .uri("/api/v1/trigger-rules")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TriggerRuleResponseDTO.class)
                .value(response -> assertThat(response).hasSize(2));
    }

    @Test
    void updateTriggerRule_whenTriggerRuleExists_returnsUpdatedTriggerRule() {
        TriggerRuleResponseDTO created = createTriggerRule("trigger-update");

        webTestClient.put()
                .uri("/api/v1/trigger-rules/{id}", created.getTriggerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(triggerRuleRequest("ignored-id", "TIME_BASED", "time == 22:00", false))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TriggerRuleResponseDTO.class)
                .value(response -> {
                    assertThat(response.getTriggerId()).isEqualTo("trigger-update");
                    assertThat(response.getTriggerType()).isEqualTo("TIME_BASED");
                    assertThat(response.isEnabled()).isFalse();
                });
    }

    @Test
    void getTriggerRuleById_whenTriggerRuleDoesNotExist_returnsNotFound() {
        webTestClient.get()
                .uri("/api/v1/trigger-rules/{id}", "missing-trigger")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").value(message -> assertThat(message.toString()).contains("TriggerRule not found"));
    }

    @Test
    void deleteTriggerRule_whenTriggerRuleExists_returnsNoContent() {
        TriggerRuleResponseDTO created = createTriggerRule("trigger-delete");

        webTestClient.delete()
                .uri("/api/v1/trigger-rules/{id}", created.getTriggerId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(triggerRuleRepository.findById(created.getTriggerId())).isEmpty();
    }

    private SceneResponseDTO createScene(String name) {
        return webTestClient.post()
                .uri("/api/v1/scenes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", name))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SceneResponseDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private TriggerRuleResponseDTO createTriggerRule(String triggerId) {
        return webTestClient.post()
                .uri("/api/v1/trigger-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(triggerRuleRequest(triggerId, "MANUAL", "manual == true", true))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TriggerRuleResponseDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private Map<String, Object> triggerRuleRequest(String triggerId, String triggerType, String conditionExpression, boolean enabled) {
        return Map.of(
                "triggerId", triggerId,
                "triggerType", triggerType,
                "conditionExpression", conditionExpression,
                "enabled", enabled
        );
    }
}
