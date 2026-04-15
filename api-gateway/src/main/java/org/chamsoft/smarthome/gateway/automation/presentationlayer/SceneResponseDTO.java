package org.chamsoft.smarthome.gateway.automation.presentationlayer;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SceneResponseDTO {

    private UUID sceneId;
    private String name;
    private String status;
    private LocalDateTime createdAt;
    private List<TriggerRuleResponseDTO> triggerRules;
    private List<DeviceActionDTO> deviceActions;

}
