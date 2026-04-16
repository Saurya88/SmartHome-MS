package org.chamsoft.smarthome.automation.dataccesslayer;

import org.chamsoft.smarthome.automation.domain.Condition;
import org.chamsoft.smarthome.automation.domain.DeviceAction;
import org.chamsoft.smarthome.automation.domain.Scene;
import org.chamsoft.smarthome.automation.domain.Schedule;
import org.chamsoft.smarthome.automation.domain.TriggerRule;
import org.chamsoft.smarthome.automation.domain.TriggerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AutomationRepositoryIntegrationTest {

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private TriggerRuleRepository triggerRuleRepository;

    @Test
    void saveScene_whenSceneHasActionsAndTriggerRules_persistsRelationships() {
        TriggerRule triggerRule = buildTriggerRule("trigger-night-mode");
        Scene scene = new Scene("Night Mode");
        scene.addDeviceAction(new DeviceAction(UUID.randomUUID(), "DIM_TO", "{\"brightness\":20}"));
        scene.addTriggerRule(triggerRule);

        Scene saved = sceneRepository.saveAndFlush(scene);

        assertThat(saved.getSceneId()).isNotNull();
        assertThat(sceneRepository.findById(saved.getSceneId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getName()).isEqualTo("Night Mode");
                    assertThat(found.getDeviceActions()).hasSize(1);
                    assertThat(found.getTriggerRules()).extracting(TriggerRule::getTriggerId)
                            .containsExactly("trigger-night-mode");
                });
    }

    @Test
    void findSceneById_whenSceneDoesNotExist_returnsEmptyOptional() {
        assertThat(sceneRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void saveTriggerRule_whenTriggerRuleIsValid_persistsTriggerRule() {
        TriggerRule triggerRule = buildTriggerRule("trigger-motion");

        TriggerRule saved = triggerRuleRepository.saveAndFlush(triggerRule);

        assertThat(triggerRuleRepository.findById(saved.getTriggerId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getTriggerType()).isEqualTo(TriggerType.SENSOR_EVENT);
                    assertThat(found.isEnabled()).isTrue();
                    assertThat(found.getCondition().getAttribute()).isEqualTo("motion");
                });
    }

    @Test
    void findTriggerRuleById_whenTriggerRuleDoesNotExist_returnsEmptyOptional() {
        assertThat(triggerRuleRepository.findById("missing-trigger")).isEmpty();
    }

    private TriggerRule buildTriggerRule(String triggerId) {
        return TriggerRule.builder()
                .triggerId(triggerId)
                .triggerType(TriggerType.SENSOR_EVENT)
                .conditionExpression("motion == true")
                .enabled(true)
                .condition(new Condition("motion", "EQUALS", "true"))
                .schedule(new Schedule(LocalDateTime.now(), LocalDateTime.now().plusHours(1), "DAILY"))
                .triggerValue("MOTION_DETECTED")
                .build();
    }
}
