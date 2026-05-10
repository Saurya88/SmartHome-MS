package org.chamsoft.smarthome.sceneorchestrator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "scene_orchestrations")
public class SceneOrchestration {

    @Id
    private UUID orchestrationId;

    private UUID ownerUserId;
    private UUID automationSceneId;
    private String name;
    private String status;
    private LocalDateTime createdAt;
    private List<ManagedDeviceAction> deviceActions;
    private List<ManagedTriggerRule> triggerRules;
}

