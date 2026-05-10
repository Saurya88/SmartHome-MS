package org.chamsoft.smarthome.sceneorchestrator.config;

import org.chamsoft.smarthome.sceneorchestrator.dataccesslayer.SceneOrchestrationRepository;
import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedDeviceAction;
import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedTriggerRule;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Profile("docker")
public class DockerDataSeeder implements CommandLineRunner {

    private final SceneOrchestrationRepository repository;

    public DockerDataSeeder(SceneOrchestrationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        seedOrchestration(
                UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                "Seed Evening Orchestration",
                "ACTIVE",
                "TURN_ON",
                "{\"brightness\":80}",
                "seed-evening-trigger",
                "time == 18:00",
                "18:00"
        );

        seedOrchestration(
                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                "Seed Morning Orchestration",
                "INACTIVE",
                "TURN_OFF",
                "{\"brightness\":0}",
                "seed-morning-trigger",
                "time == 07:00",
                "07:00"
        );
    }

    private void seedOrchestration(UUID orchestrationId,
                                   UUID automationSceneId,
                                   String name,
                                   String status,
                                   String actionType,
                                   String parameters,
                                   String triggerId,
                                   String conditionExpression,
                                   String triggerValue) {
        if (repository.existsById(orchestrationId)) {
            return;
        }

        repository.save(SceneOrchestration.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .automationSceneId(automationSceneId)
                .name(name)
                .status(status)
                .createdAt(LocalDateTime.of(2026, 1, 1, 9, 30))
                .deviceActions(List.of(ManagedDeviceAction.builder()
                        .deviceId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                        .actionType(actionType)
                        .parameters(parameters)
                        .build()))
                .triggerRules(List.of(ManagedTriggerRule.builder()
                        .triggerId(triggerId)
                        .triggerType("TIME_BASED")
                        .conditionExpression(conditionExpression)
                        .enabled(true)
                        .triggerValue(triggerValue)
                        .build()))
                .build());
    }
}
