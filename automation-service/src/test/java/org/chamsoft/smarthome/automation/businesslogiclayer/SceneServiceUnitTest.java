package org.chamsoft.smarthome.automation.businesslogiclayer;

import org.chamsoft.smarthome.automation.dataccesslayer.SceneRepository;
import org.chamsoft.smarthome.automation.domain.DeviceAction;
import org.chamsoft.smarthome.automation.domain.Scene;
import org.chamsoft.smarthome.automation.domain.SceneStatus;
import org.chamsoft.smarthome.automation.domain.TriggerRule;
import org.chamsoft.smarthome.automation.domain.TriggerType;
import org.chamsoft.smarthome.automation.presentationlayer.SceneRequestDTO;
import org.chamsoft.smarthome.automation.utilities.InvalidSceneActivationException;
import org.chamsoft.smarthome.automation.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SceneServiceUnitTest {

    @Mock
    private SceneRepository sceneRepository;

    @InjectMocks
    private SceneService sceneService;

    @Test
    void activateScene_whenSceneHasNoActionsOrRules_throwsInvalidSceneActivationException() {
        Scene scene = new Scene("Incomplete Scene");
        when(sceneRepository.findById(scene.getSceneId())).thenReturn(Optional.of(scene));

        assertThatThrownBy(() -> sceneService.activateScene(scene.getSceneId()))
                .isInstanceOf(InvalidSceneActivationException.class)
                .hasMessage("Scene must contain at least one TriggerRule and one DeviceAction to activate.");
    }

    @Test
    void activateScene_whenSceneIsComplete_marksSceneActive() {
        Scene scene = completeScene();
        when(sceneRepository.findById(scene.getSceneId())).thenReturn(Optional.of(scene));
        when(sceneRepository.save(scene)).thenReturn(scene);

        Scene activated = sceneService.activateScene(scene.getSceneId());

        assertThat(activated.getStatus()).isEqualTo(SceneStatus.ACTIVE);
        verify(sceneRepository).save(scene);
    }

    @Test
    void getSceneOrThrow_whenMissing_throwsResourceNotFoundException() {
        UUID sceneId = UUID.randomUUID();
        when(sceneRepository.findById(sceneId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sceneService.getSceneOrThrow(sceneId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Scene not found");
    }

    @Test
    void updateScene_whenSceneExists_updatesNameAndStatus() {
        Scene scene = new Scene("Old Scene");
        SceneRequestDTO requestDTO = new SceneRequestDTO();
        requestDTO.setName("Updated Scene");
        requestDTO.setStatus(SceneStatus.SCHEDULED);

        when(sceneRepository.findById(scene.getSceneId())).thenReturn(Optional.of(scene));
        when(sceneRepository.save(scene)).thenReturn(scene);

        Scene updated = sceneService.updateScene(scene.getSceneId(), requestDTO);

        assertThat(updated.getName()).isEqualTo("Updated Scene");
        assertThat(updated.getStatus()).isEqualTo(SceneStatus.SCHEDULED);
    }

    @Test
    void replaceDeviceActions_whenSceneExists_replacesExistingActions() {
        Scene scene = completeScene();
        DeviceAction replacement = new DeviceAction(UUID.randomUUID(), "TURN_OFF", "{}");

        when(sceneRepository.findById(scene.getSceneId())).thenReturn(Optional.of(scene));
        when(sceneRepository.save(scene)).thenReturn(scene);

        Scene updated = sceneService.replaceDeviceActions(scene.getSceneId(), List.of(replacement));

        assertThat(updated.getDeviceActions()).containsExactly(replacement);
    }

    @Test
    void replaceTriggerRules_whenSceneExists_replacesExistingRules() {
        Scene scene = completeScene();
        TriggerRule replacement = TriggerRule.builder()
                .triggerId("replacement-trigger")
                .triggerType(TriggerType.TIME_BASED)
                .conditionExpression("time == 23:00")
                .enabled(true)
                .build();

        when(sceneRepository.findById(scene.getSceneId())).thenReturn(Optional.of(scene));
        when(sceneRepository.save(scene)).thenReturn(scene);

        Scene updated = sceneService.replaceTriggerRules(scene.getSceneId(), List.of(replacement));

        assertThat(updated.getTriggerRules()).containsExactly(replacement);
    }

    private Scene completeScene() {
        Scene scene = new Scene("Complete Scene");
        scene.addDeviceAction(new DeviceAction(UUID.randomUUID(), "TURN_ON", "{}"));
        scene.addTriggerRule(TriggerRule.builder()
                .triggerId("manual-trigger")
                .triggerType(TriggerType.MANUAL)
                .conditionExpression("manual == true")
                .enabled(true)
                .build());
        return scene;
    }
}
