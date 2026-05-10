package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutomationSceneRequestDTO {
    private String name;
    private String status;
}
