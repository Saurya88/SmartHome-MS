package org.chamsoft.smarthome.automation.config;

import org.chamsoft.smarthome.automation.dataccesslayer.SceneRepository;
import org.chamsoft.smarthome.automation.dataccesslayer.TriggerRuleRepository;
import org.chamsoft.smarthome.automation.domain.DeviceAction;
import org.chamsoft.smarthome.automation.domain.Scene;
import org.chamsoft.smarthome.automation.domain.TriggerRule;
import org.chamsoft.smarthome.automation.domain.TriggerType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("docker")
public class DockerDataSeeder implements CommandLineRunner {

    private final SceneRepository sceneRepository;
    private final TriggerRuleRepository triggerRuleRepository;

    public DockerDataSeeder(SceneRepository sceneRepository, TriggerRuleRepository triggerRuleRepository) {
        this.sceneRepository = sceneRepository;
        this.triggerRuleRepository = triggerRuleRepository;
    }

    @Override
    public void run(String... args) {
        TriggerRule eveningRule = triggerRuleRepository.findById("seed-evening-trigger")
                .orElseGet(() -> triggerRuleRepository.save(TriggerRule.builder()
                        .triggerId("seed-evening-trigger")
                        .triggerType(TriggerType.TIME_BASED)
                        .conditionExpression("time == 18:00")
                        .enabled(true)
                        .triggerValue("18:00")
                        .build()));

        seedScene(
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                "Seed Evening Scene",
                eveningRule
        );

        seedScene(
                UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                "Seed Morning Scene",
                null
        );
    }

    private void seedScene(UUID sceneId, String name, TriggerRule triggerRule) {
        if (sceneRepository.existsById(sceneId)) {
            return;
        }

        Scene scene = new Scene(name);
        scene.setSceneId(sceneId);

        if (triggerRule != null) {
            scene.addTriggerRule(triggerRule);
            scene.addDeviceAction(new DeviceAction(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    "TURN_ON",
                    "brightness=80"
            ));
        }

        sceneRepository.save(scene);
    }
}
