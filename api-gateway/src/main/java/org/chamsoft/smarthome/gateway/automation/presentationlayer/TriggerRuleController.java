package org.chamsoft.smarthome.gateway.automation.presentationlayer;

import org.chamsoft.smarthome.gateway.automation.businesslogiclayer.TriggerRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trigger-rules")
public class TriggerRuleController {

    private final TriggerRuleService triggerRuleService;

    public TriggerRuleController(TriggerRuleService triggerRuleService) {
        this.triggerRuleService = triggerRuleService;
    }

    @GetMapping
    public ResponseEntity<List<TriggerRuleResponseDTO>> getAllTriggerRules() {
        return ResponseEntity.ok(triggerRuleService.getAllTriggerRules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TriggerRuleResponseDTO> getTriggerRuleById(@PathVariable String id) {
        return ResponseEntity.ok(triggerRuleService.getTriggerRuleById(id));
    }

    @PostMapping
    public ResponseEntity<TriggerRuleResponseDTO> createTriggerRule(@RequestBody TriggerRuleRequestDTO requestDTO) {
        TriggerRuleResponseDTO created = triggerRuleService.createTriggerRule(requestDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getTriggerId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TriggerRuleResponseDTO> updateTriggerRule(@PathVariable String id,
                                                                    @RequestBody TriggerRuleRequestDTO requestDTO) {
        return ResponseEntity.ok(triggerRuleService.updateTriggerRule(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTriggerRule(@PathVariable String id) {
        triggerRuleService.deleteTriggerRule(id);
        return ResponseEntity.noContent().build();
    }
}