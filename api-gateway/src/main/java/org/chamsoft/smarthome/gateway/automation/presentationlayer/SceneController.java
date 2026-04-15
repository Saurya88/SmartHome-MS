package org.chamsoft.smarthome.gateway.automation.presentationlayer;

import org.chamsoft.smarthome.gateway.automation.businesslogiclayer.SceneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scenes")
public class SceneController {

    private final SceneService sceneService;

    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    @GetMapping
    public ResponseEntity<List<SceneResponseDTO>> getAllScenes() {
        return ResponseEntity.ok(sceneService.getAllScenes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SceneResponseDTO> getSceneById(@PathVariable UUID id) {
        return ResponseEntity.ok(sceneService.getSceneById(id));
    }

    @PostMapping
    public ResponseEntity<SceneResponseDTO> createScene(@RequestBody SceneRequestDTO requestDTO) {
        SceneResponseDTO created = sceneService.createScene(requestDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getSceneId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SceneResponseDTO> updateScene(@PathVariable UUID id,
                                                        @RequestBody SceneRequestDTO requestDTO) {
        return ResponseEntity.ok(sceneService.updateScene(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable UUID id) {
        sceneService.deleteScene(id);
        return ResponseEntity.noContent().build();
    }
}