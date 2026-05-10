package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationSceneRequestDTO {
    private String name;
    private String status;
    private List<AutomationTriggerRuleRequestDTO> triggerRules;
    private List<AutomationDeviceActionRequestDTO> deviceActions;
}

