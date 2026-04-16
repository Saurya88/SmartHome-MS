package org.chamsoft.smarthome.gateway.automation.presentationlayer;

import org.chamsoft.smarthome.gateway.automation.domainclientlayer.SceneService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/scenes")
public class SceneController {

    private final SceneService sceneService;

    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<SceneResponseDTO>>> getAllScenes() {
        List<EntityModel<SceneResponseDTO>> scenes = sceneService.getAllScenes().stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(
                scenes,
                linkTo(methodOn(SceneController.class).getAllScenes()).withSelfRel()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<SceneResponseDTO>> getSceneById(@PathVariable UUID id) {
        return ResponseEntity.ok(toModel(sceneService.getSceneById(id)));
    }

    @PostMapping
    public ResponseEntity<EntityModel<SceneResponseDTO>> createScene(@RequestBody SceneRequestDTO requestDTO) {
        SceneResponseDTO created = sceneService.createScene(requestDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getSceneId())
                .toUri();

        return ResponseEntity.created(location).body(toModel(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<SceneResponseDTO>> updateScene(@PathVariable UUID id,
                                                                     @RequestBody SceneRequestDTO requestDTO) {
        return ResponseEntity.ok(toModel(sceneService.updateScene(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable UUID id) {
        sceneService.deleteScene(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<SceneResponseDTO> toModel(SceneResponseDTO scene) {
        return EntityModel.of(
                scene,
                linkTo(methodOn(SceneController.class).getSceneById(scene.getSceneId())).withSelfRel(),
                linkTo(methodOn(SceneController.class).getAllScenes()).withRel("scenes")
        );
    }
}
