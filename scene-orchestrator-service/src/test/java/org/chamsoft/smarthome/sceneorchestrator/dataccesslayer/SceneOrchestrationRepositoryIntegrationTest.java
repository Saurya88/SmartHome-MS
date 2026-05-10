package org.chamsoft.smarthome.sceneorchestrator.dataccesslayer;

import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedDeviceAction;
import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedTriggerRule;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.chamsoft.smarthome.sceneorchestrator.testutils.EmbeddedMongoSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest(properties = {
        "spring.data.mongodb.database=scene_orchestrator_repo_test"
})
class SceneOrchestrationRepositoryIntegrationTest {

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:" + EmbeddedMongoSupport.port() + "/scene_orchestrator_repo_test");
    }

    @Autowired
    private SceneOrchestrationRepository repository;

    @Test
    void save_whenValidEntity_persistsAndLoadsById() {
        UUID id = UUID.randomUUID();
        SceneOrchestration entity = SceneOrchestration.builder()
                .orchestrationId(id)
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(UUID.randomUUID())
                .name("Night Routine")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of(ManagedDeviceAction.builder()
                        .deviceId(UUID.randomUUID())
                        .actionType("LOCK")
                        .parameters("{}")
                        .build()))
                .triggerRules(List.of(ManagedTriggerRule.builder()
                        .triggerId("rule-1")
                        .triggerType("TIME_BASED")
                        .conditionExpression("time == 22:00")
                        .enabled(true)
                        .triggerValue("22:00")
                        .build()))
                .build();

        repository.save(entity);

        SceneOrchestration loaded = repository.findById(id).orElse(null);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getName()).isEqualTo("Night Routine");
        assertThat(loaded.getDeviceActions()).hasSize(1);
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAll_whenMultipleSaved_returnsAll() {
        repository.deleteAll();

        SceneOrchestration first = SceneOrchestration.builder()
                .orchestrationId(UUID.randomUUID())
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(UUID.randomUUID())
                .name("First")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of(ManagedDeviceAction.builder()
                        .deviceId(UUID.randomUUID())
                        .actionType("LOCK")
                        .parameters("{}")
                        .build()))
                .triggerRules(List.of(ManagedTriggerRule.builder()
                        .triggerId("rule-1")
                        .triggerType("TIME_BASED")
                        .conditionExpression("x")
                        .enabled(true)
                        .triggerValue("y")
                        .build()))
                .build();

        SceneOrchestration second = SceneOrchestration.builder()
                .orchestrationId(UUID.randomUUID())
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(UUID.randomUUID())
                .name("Second")
                .status("INACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of(ManagedDeviceAction.builder()
                        .deviceId(UUID.randomUUID())
                        .actionType("UNLOCK")
                        .parameters("{}")
                        .build()))
                .triggerRules(List.of(ManagedTriggerRule.builder()
                        .triggerId("rule-2")
                        .triggerType("MANUAL")
                        .conditionExpression("x")
                        .enabled(false)
                        .triggerValue("y")
                        .build()))
                .build();

        repository.save(first);
        repository.save(second);

        assertThat(repository.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }
}
