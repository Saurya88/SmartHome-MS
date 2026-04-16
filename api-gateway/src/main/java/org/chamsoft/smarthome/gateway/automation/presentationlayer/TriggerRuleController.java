package org.chamsoft.smarthome.gateway.automation.presentationlayer;

import org.chamsoft.smarthome.gateway.automation.domainclientlayer.TriggerRuleService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/trigger-rules")
public class TriggerRuleController {

    private final TriggerRuleService triggerRuleService;

    public TriggerRuleController(TriggerRuleService triggerRuleService) {
        this.triggerRuleService = triggerRuleService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<TriggerRuleResponseDTO>>> getAllTriggerRules() {
        List<EntityModel<TriggerRuleResponseDTO>> triggerRules = triggerRuleService.getAllTriggerRules().stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(
                triggerRules,
                linkTo(methodOn(TriggerRuleController.class).getAllTriggerRules()).withSelfRel()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<TriggerRuleResponseDTO>> getTriggerRuleById(@PathVariable String id) {
        return ResponseEntity.ok(toModel(triggerRuleService.getTriggerRuleById(id)));
    }

    @PostMapping
    public ResponseEntity<EntityModel<TriggerRuleResponseDTO>> createTriggerRule(@RequestBody TriggerRuleRequestDTO requestDTO) {
        TriggerRuleResponseDTO created = triggerRuleService.createTriggerRule(requestDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getTriggerId())
                .toUri();

        return ResponseEntity.created(location).body(toModel(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<TriggerRuleResponseDTO>> updateTriggerRule(@PathVariable String id,
                                                                                 @RequestBody TriggerRuleRequestDTO requestDTO) {
        return ResponseEntity.ok(toModel(triggerRuleService.updateTriggerRule(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTriggerRule(@PathVariable String id) {
        triggerRuleService.deleteTriggerRule(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<TriggerRuleResponseDTO> toModel(TriggerRuleResponseDTO triggerRule) {
        return EntityModel.of(
                triggerRule,
                linkTo(methodOn(TriggerRuleController.class).getTriggerRuleById(triggerRule.getTriggerId())).withSelfRel(),
                linkTo(methodOn(TriggerRuleController.class).getAllTriggerRules()).withRel("trigger-rules")
        );
    }
}
