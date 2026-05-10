package org.chamsoft.smarthome.sceneorchestrator.dataccesslayer;

import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedDeviceAction;
import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedTriggerRule;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestrationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class SceneOrchestrationRepositoryIntegrationTest {

    @Autowired
    private SceneOrchestrationRepository repository;

    @Test
    void saveSceneOrchestration_whenValid_persistsAggregateState() {
        UUID deviceId = UUID.randomUUID();

        SceneOrchestration orchestration = SceneOrchestration.builder()
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(UUID.randomUUID())
                .name("Night Lockdown")
                .status(SceneOrchestrationStatus.SCHEDULED)
                .deviceActions(List.of(ManagedDeviceAction.builder()
                        .deviceId(deviceId)
                        .actionType("LOCK")
                        .parameters("{\"delay\":0}")
                        .build()))
                .triggerRules(List.of(ManagedTriggerRule.builder()
                        .triggerId("night-trigger")
                        .triggerType("TIME_BASED")
                        .conditionExpression("time == 22:00")
                        .enabled(true)
                        .triggerValue("22:00")
                        .build()))
                .build();

        SceneOrchestration saved = repository.saveAndFlush(orchestration);

        assertThat(repository.findById(saved.getOrchestrationId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getName()).isEqualTo("Night Lockdown");
                    assertThat(found.getStatus()).isEqualTo(SceneOrchestrationStatus.SCHEDULED);
                    assertThat(found.getDeviceActions()).hasSize(1);
                    assertThat(found.getTriggerRules()).hasSize(1);
                    assertThat(found.getDeviceActions().getFirst().getDeviceId()).isEqualTo(deviceId);
                });
    }

    @Test
    void findById_whenMissing_returnsEmptyOptional() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }
}
