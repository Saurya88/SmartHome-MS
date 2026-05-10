package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AutomationSceneResponseDTO {
    private UUID sceneId;
    private String name;
    private String status;
}
