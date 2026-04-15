package org.chamsoft.smarthome.automation.presentationlayer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.chamsoft.smarthome.automation.domain.SceneStatus;

@Getter
@Setter
public class SceneRequestDTO {

    @NotBlank(message = "Scene name is required")
    private String name;
    private SceneStatus status;
}