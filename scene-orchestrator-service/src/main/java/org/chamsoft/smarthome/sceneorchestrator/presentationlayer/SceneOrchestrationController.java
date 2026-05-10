package org.chamsoft.smarthome.sceneorchestrator.presentationlayer;

import jakarta.validation.Valid;
import org.chamsoft.smarthome.sceneorchestrator.businesslogiclayer.SceneOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scene-orchestrations")
public class SceneOrchestrationController {

    private final SceneOrchestrationService sceneOrchestrationService;

    public SceneOrchestrationController(SceneOrchestrationService sceneOrchestrationService) {
        this.sceneOrchestrationService = sceneOrchestrationService;
    }

    @GetMapping
    public ResponseEntity<List<SceneOrchestrationResponseDTO>> getAllSceneOrchestrations() {
        return ResponseEntity.ok(sceneOrchestrationService.getAllSceneOrchestrations());
    }

    @GetMapping("/{orchestrationId}")
    public ResponseEntity<SceneOrchestrationResponseDTO> getSceneOrchestrationById(@PathVariable UUID orchestrationId) {
        return ResponseEntity.ok(sceneOrchestrationService.getSceneOrchestrationById(orchestrationId));
    }

    @PostMapping
    public ResponseEntity<SceneOrchestrationResponseDTO> createSceneOrchestration(
            @Valid @RequestBody SceneOrchestrationRequestDTO requestDTO) {
        SceneOrchestrationResponseDTO created = sceneOrchestrationService.createSceneOrchestration(requestDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orchestrationId}")
                .buildAndExpand(created.getOrchestrationId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{orchestrationId}")
    public ResponseEntity<SceneOrchestrationResponseDTO> updateSceneOrchestration(
            @PathVariable UUID orchestrationId,
            @Valid @RequestBody SceneOrchestrationRequestDTO requestDTO) {
        return ResponseEntity.ok(sceneOrchestrationService.updateSceneOrchestration(orchestrationId, requestDTO));
    }

    @DeleteMapping("/{orchestrationId}")
    public ResponseEntity<Void> deleteSceneOrchestration(@PathVariable UUID orchestrationId) {
        sceneOrchestrationService.deleteSceneOrchestration(orchestrationId);
        return ResponseEntity.noContent().build();
    }
}

