package org.chamsoft.smarthome.gateway.automation.presentationlayer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SceneRequestDTO {

    @NotBlank(message = "Scene name is required")
    private String name;
    private String status;
}