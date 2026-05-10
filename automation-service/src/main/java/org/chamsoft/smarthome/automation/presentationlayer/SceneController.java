package org.chamsoft.smarthome.automation.presentationlayer;

import jakarta.validation.Valid;
import org.chamsoft.smarthome.automation.businesslogiclayer.SceneService;
import org.chamsoft.smarthome.automation.datamappinglayer.*;
import org.chamsoft.smarthome.automation.domain.DeviceAction;
import org.chamsoft.smarthome.automation.domain.TriggerRule;
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
        return ResponseEntity.ok(
                sceneService.getAllScenes().stream()
                        .map(SceneMapper::toDTO)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SceneResponseDTO> getScene(@PathVariable UUID id) {
        return ResponseEntity.ok(
                SceneMapper.toDTO(sceneService.getSceneOrThrow(id))
        );
    }

    @PostMapping
    public ResponseEntity<SceneResponseDTO> createScene(@Valid @RequestBody SceneRequestDTO request) {
        var created = sceneService.createScene(request.getName());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getSceneId())
                .toUri();

        return ResponseEntity.created(location)
                .body(SceneMapper.toDTO(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SceneResponseDTO> updateScene(
            @PathVariable UUID id,
            @Valid @RequestBody SceneRequestDTO request) {

        return ResponseEntity.ok(
                SceneMapper.toDTO(sceneService.updateScene(id, request))
        );
    }

    @PostMapping("/{id}/device-actions")
    public ResponseEntity<SceneResponseDTO> addDeviceActions(
            @PathVariable UUID id,
            @Valid @RequestBody List<DeviceActionRequestDTO> actions) {

        return ResponseEntity.ok(
                SceneMapper.toDTO(sceneService.addDeviceActions(
                        id,
                        actions.stream()
                                .map(action -> new DeviceAction(action.getDeviceId(), action.getActionType(), action.getParameters()))
                                .toList()
                ))
        );
    }

    @PostMapping("/{id}/trigger-rules")
    public ResponseEntity<SceneResponseDTO> addTriggerRules(
            @PathVariable UUID id,
            @Valid @RequestBody List<TriggerRuleRequestDTO> rules) {

        return ResponseEntity.ok(
                SceneMapper.toDTO(sceneService.addTriggerRules(
                        id,
                        rules.stream()
                                .map(TriggerRuleMapper::toEntity)
                                .toList()
                ))
        );
    }

    @PutMapping("/{id}/device-actions")
    public ResponseEntity<SceneResponseDTO> replaceDeviceActions(
            @PathVariable UUID id,
            @Valid @RequestBody List<DeviceActionRequestDTO> actions) {
        return ResponseEntity.ok(
                SceneMapper.toDTO(sceneService.replaceDeviceActions(id, actions))
        );
    }

    @PutMapping("/{id}/trigger-rules")
    public ResponseEntity<SceneResponseDTO> replaceTriggerRules(
            @PathVariable UUID id,
            @Valid @RequestBody List<TriggerRuleRequestDTO> rules) {
        return ResponseEntity.ok(
                SceneMapper.toDTO(sceneService.replaceTriggerRules(id, rules))
        );
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<SceneResponseDTO> activateScene(@PathVariable UUID id) {
        return ResponseEntity.ok(
                SceneMapper.toDTO(sceneService.activateScene(id))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable UUID id) {
        sceneService.deleteScene(id);
        return ResponseEntity.noContent().build();
    }
}
