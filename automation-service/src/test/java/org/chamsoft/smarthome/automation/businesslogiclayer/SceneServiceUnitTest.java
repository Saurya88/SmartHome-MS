package org.chamsoft.smarthome.automation.businesslogiclayer;

import org.chamsoft.smarthome.automation.dataccesslayer.SceneRepository;
import org.chamsoft.smarthome.automation.domain.DeviceAction;
import org.chamsoft.smarthome.automation.domain.Scene;
import org.chamsoft.smarthome.automation.domain.SceneStatus;
import org.chamsoft.smarthome.automation.presentationlayer.DeviceActionRequestDTO;
import org.chamsoft.smarthome.automation.presentationlayer.SceneRequestDTO;
import org.chamsoft.smarthome.automation.presentationlayer.TriggerRuleRequestDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SceneServiceUnitTest {

    @Mock
    private SceneRepository sceneRepository;

    @InjectMocks
    private SceneService sceneService;

    @Test
    void updateScene_whenStatusIsActiveAndInvariantSatisfied_returnsActiveScene() {
        UUID sceneId = UUID.randomUUID();
        Scene scene = new Scene("Night Routine");
        scene.addDeviceAction(new DeviceAction(UUID.randomUUID(), "LOCK", "{}"));
        scene.addTriggerRule(org.chamsoft.smarthome.automation.domain.TriggerRule.builder()
                .triggerId("night-trigger")
                .triggerType(org.chamsoft.smarthome.automation.domain.TriggerType.TIME_BASED)
                .conditionExpression("time == 22:00")
                .enabled(true)
                .build());

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(scene));
        when(sceneRepository.save(scene)).thenReturn(scene);

        SceneRequestDTO requestDTO = new SceneRequestDTO();
        requestDTO.setName("Night Routine");
        requestDTO.setStatus(SceneStatus.ACTIVE);

        Scene updated = sceneService.updateScene(sceneId, requestDTO);

        assertThat(updated.getStatus()).isEqualTo(SceneStatus.ACTIVE);
    }

    @Test
    void updateScene_whenStatusIsActiveAndInvariantFails_throwsInvalidActivation() {
        UUID sceneId = UUID.randomUUID();
        Scene scene = new Scene("Incomplete Scene");

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(scene));

        SceneRequestDTO requestDTO = new SceneRequestDTO();
        requestDTO.setName("Incomplete Scene");
        requestDTO.setStatus(SceneStatus.ACTIVE);

        assertThatThrownBy(() -> sceneService.updateScene(sceneId, requestDTO))
                .isInstanceOf(InvalidSceneActivationException.class)
                .hasMessageContaining("Scene must contain at least one TriggerRule and one DeviceAction");
    }

    @Test
    void replaceDeviceActions_whenSceneExists_replacesAllActions() {
        UUID sceneId = UUID.randomUUID();
        Scene scene = new Scene("Night Routine");
        scene.addDeviceAction(new DeviceAction(UUID.randomUUID(), "OLD", "{}"));

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(scene));
        when(sceneRepository.save(scene)).thenReturn(scene);

        Scene updated = sceneService.replaceDeviceActions(sceneId, List.of(
                new DeviceActionRequestDTO(UUID.randomUUID(), "LOCK", "{}")
        ));

        assertThat(updated.getDeviceActions()).hasSize(1);
        assertThat(updated.getDeviceActions().getFirst().getActionType()).isEqualTo("LOCK");
    }

    @Test
    void replaceTriggerRules_whenSceneExists_replacesAllRules() {
        UUID sceneId = UUID.randomUUID();
        Scene scene = new Scene("Night Routine");

        when(sceneRepository.findById(sceneId)).thenReturn(Optional.of(scene));
        when(sceneRepository.save(scene)).thenReturn(scene);

        Scene updated = sceneService.replaceTriggerRules(sceneId, List.of(
                TriggerRuleRequestDTO.builder()
                        .triggerId("night-trigger")
                        .triggerType("TIME_BASED")
                        .conditionExpression("time == 22:00")
                        .enabled(true)
                        .triggerValue("22:00")
                        .build()
        ));

        assertThat(updated.getTriggerRules()).hasSize(1);
        assertThat(updated.getTriggerRules().getFirst().getTriggerId()).isEqualTo("night-trigger");
    }

    @Test
    void getSceneOrThrow_whenSceneDoesNotExist_throwsNotFound() {
        UUID sceneId = UUID.randomUUID();
        when(sceneRepository.findById(sceneId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sceneService.getSceneOrThrow(sceneId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Scene not found");
    }

    @Test
    void deleteScene_whenSceneExists_deletesById() {
        UUID sceneId = UUID.randomUUID();

        sceneService.deleteScene(sceneId);

        verify(sceneRepository).deleteById(sceneId);
    }
}
