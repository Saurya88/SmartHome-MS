package org.chamsoft.smarthome.sceneorchestrator.datamappinglayer;

import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedDeviceAction;
import org.chamsoft.smarthome.sceneorchestrator.domain.ManagedTriggerRule;
import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.chamsoft.smarthome.sceneorchestrator.presentationlayer.SceneOrchestrationRequestDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SceneOrchestrationMapperTest {

    @Test
    void toNewEntity_mapsRequestToEntity() {
        UUID orchestrationId = UUID.randomUUID();
        UUID automationSceneId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        SceneOrchestrationRequestDTO request = SceneOrchestrationRequestDTO.builder()
                .ownerUserId(ownerUserId)
                .name("Night Routine")
                .status("ACTIVE")
                .deviceActions(List.of(SceneOrchestrationRequestDTO.DeviceActionRequestDTO.builder()
                        .deviceId(deviceId)
                        .actionType("LOCK")
                        .parameters("{}")
                        .build()))
                .triggerRules(List.of(SceneOrchestrationRequestDTO.TriggerRuleRequestDTO.builder()
                        .triggerId("rule-1")
                        .triggerType("TIME_BASED")
                        .conditionExpression("time == 22:00")
                        .enabled(true)
                        .triggerValue("22:00")
                        .build()))
                .build();

        SceneOrchestration entity = SceneOrchestrationMapper.toNewEntity(request, orchestrationId, automationSceneId);

        assertThat(entity.getOrchestrationId()).isEqualTo(orchestrationId);
        assertThat(entity.getAutomationSceneId()).isEqualTo(automationSceneId);
        assertThat(entity.getOwnerUserId()).isEqualTo(ownerUserId);
        assertThat(entity.getName()).isEqualTo("Night Routine");
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getDeviceActions()).hasSize(1);
        assertThat(entity.getDeviceActions().getFirst().getDeviceId()).isEqualTo(deviceId);
        assertThat(entity.getTriggerRules()).hasSize(1);
        assertThat(entity.getTriggerRules().getFirst().getTriggerId()).isEqualTo("rule-1");
    }

    @Test
    void updateEntity_overwritesMutableFields() {
        SceneOrchestration entity = SceneOrchestration.builder()
                .orchestrationId(UUID.randomUUID())
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(UUID.randomUUID())
                .name("Old")
                .status("INACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of(ManagedDeviceAction.builder()
                        .deviceId(UUID.randomUUID())
                        .actionType("OLD")
                        .parameters("{}")
                        .build()))
                .triggerRules(List.of(ManagedTriggerRule.builder()
                        .triggerId("old-rule")
                        .triggerType("TIME_BASED")
                        .conditionExpression("x")
                        .enabled(false)
                        .triggerValue("00:00")
                        .build()))
                .build();

        UUID newOwner = UUID.randomUUID();
        UUID newDeviceId = UUID.randomUUID();
        SceneOrchestrationRequestDTO request = SceneOrchestrationRequestDTO.builder()
                .ownerUserId(newOwner)
                .name("New Name")
                .status("ACTIVE")
                .deviceActions(List.of(SceneOrchestrationRequestDTO.DeviceActionRequestDTO.builder()
                        .deviceId(newDeviceId)
                        .actionType("LOCK")
                        .parameters("{\"a\":1}")
                        .build()))
                .triggerRules(List.of(SceneOrchestrationRequestDTO.TriggerRuleRequestDTO.builder()
                        .triggerId("rule-1")
                        .triggerType("SENSOR_EVENT")
                        .conditionExpression("temp > 20")
                        .enabled(true)
                        .triggerValue("20")
                        .build()))
                .build();

        SceneOrchestrationMapper.updateEntity(entity, request);

        assertThat(entity.getOwnerUserId()).isEqualTo(newOwner);
        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getDeviceActions()).hasSize(1);
        assertThat(entity.getTriggerRules()).hasSize(1);
    }

    @Test
    void toResponseDTO_mapsNestedCollections() {
        UUID orchestrationId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        UUID automationSceneId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        SceneOrchestration entity = SceneOrchestration.builder()
                .orchestrationId(orchestrationId)
                .ownerUserId(ownerUserId)
                .automationSceneId(automationSceneId)
                .name("Night Routine")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of(ManagedDeviceAction.builder()
                        .deviceId(deviceId)
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

        var dto = SceneOrchestrationMapper.toResponseDTO(entity);

        assertThat(dto.getOrchestrationId()).isEqualTo(orchestrationId);
        assertThat(dto.getOwnerUserId()).isEqualTo(ownerUserId);
        assertThat(dto.getAutomationSceneId()).isEqualTo(automationSceneId);
        assertThat(dto.getDeviceActions()).hasSize(1);
        assertThat(dto.getDeviceActions().getFirst().getDeviceId()).isEqualTo(deviceId);
        assertThat(dto.getTriggerRules()).hasSize(1);
        assertThat(dto.getTriggerRules().getFirst().getTriggerId()).isEqualTo("rule-1");
    }
}

