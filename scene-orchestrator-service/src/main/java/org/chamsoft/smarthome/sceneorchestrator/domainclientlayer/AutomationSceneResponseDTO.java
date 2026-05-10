package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.Data;

import java.util.UUID;

@Data
public class AutomationSceneResponseDTO {
    private UUID sceneId;
    private String name;
    private String status;
}

