package org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer;

import jakarta.validation.Valid;
import org.chamsoft.smarthome.gateway.sceneorchestration.domainclientlayer.SceneOrchestrationService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/scene-orchestrations")
public class SceneOrchestrationController {

    private final SceneOrchestrationService sceneOrchestrationService;

    public SceneOrchestrationController(SceneOrchestrationService sceneOrchestrationService) {
        this.sceneOrchestrationService = sceneOrchestrationService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<SceneOrchestrationResponseDTO>>> getAllSceneOrchestrations() {
        List<EntityModel<SceneOrchestrationResponseDTO>> orchestrations = sceneOrchestrationService.getAllSceneOrchestrations()
                .stream()
                .map(this::toModel)
                .toList();

        return ResponseEntity.ok(CollectionModel.of(
                orchestrations,
                linkTo(methodOn(SceneOrchestrationController.class).getAllSceneOrchestrations()).withSelfRel()
        ));
    }

    @GetMapping("/{orchestrationId}")
    public ResponseEntity<EntityModel<SceneOrchestrationResponseDTO>> getSceneOrchestrationById(
            @PathVariable UUID orchestrationId) {
        return ResponseEntity.ok(toModel(sceneOrchestrationService.getSceneOrchestrationById(orchestrationId)));
    }

    @PostMapping
    public ResponseEntity<EntityModel<SceneOrchestrationResponseDTO>> createSceneOrchestration(
            @Valid @RequestBody SceneOrchestrationRequestDTO requestDTO) {
        SceneOrchestrationResponseDTO created = sceneOrchestrationService.createSceneOrchestration(requestDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orchestrationId}")
                .buildAndExpand(created.getOrchestrationId())
                .toUri();

        return ResponseEntity.created(location).body(toModel(created));
    }

    @PutMapping("/{orchestrationId}")
    public ResponseEntity<EntityModel<SceneOrchestrationResponseDTO>> updateSceneOrchestration(
            @PathVariable UUID orchestrationId,
            @Valid @RequestBody SceneOrchestrationRequestDTO requestDTO) {
        return ResponseEntity.ok(toModel(sceneOrchestrationService.updateSceneOrchestration(orchestrationId, requestDTO)));
    }

    @DeleteMapping("/{orchestrationId}")
    public ResponseEntity<Void> deleteSceneOrchestration(@PathVariable UUID orchestrationId) {
        sceneOrchestrationService.deleteSceneOrchestration(orchestrationId);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<SceneOrchestrationResponseDTO> toModel(SceneOrchestrationResponseDTO orchestration) {
        return EntityModel.of(
                orchestration,
                linkTo(methodOn(SceneOrchestrationController.class)
                        .getSceneOrchestrationById(orchestration.getOrchestrationId())).withSelfRel(),
                linkTo(methodOn(SceneOrchestrationController.class).getAllSceneOrchestrations()).withRel("scene-orchestrations")
        );
    }
}

