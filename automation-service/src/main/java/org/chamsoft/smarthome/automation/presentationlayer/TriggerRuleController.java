package org.chamsoft.smarthome.automation.presentationlayer;

import org.chamsoft.smarthome.automation.businesslogiclayer.TriggerRuleService;
import org.chamsoft.smarthome.automation.datamappinglayer.TriggerRuleMapper;
import org.chamsoft.smarthome.automation.domain.TriggerRule;
import org.chamsoft.smarthome.automation.domain.TriggerType;
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
        return ResponseEntity.ok(
                triggerRuleService.getAllTriggerRules().stream()
                        .map(TriggerRuleMapper::toDTO)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TriggerRuleResponseDTO> getTriggerRule(@PathVariable String id) {
        return ResponseEntity.ok(
                TriggerRuleMapper.toDTO(triggerRuleService.getTriggerRuleById(id))
        );
    }

    @PostMapping
    public ResponseEntity<TriggerRuleResponseDTO> createTriggerRule(
            @RequestBody TriggerRuleRequestDTO request) {

        TriggerRule saved = triggerRuleService.saveTriggerRule(
                TriggerRuleMapper.toEntity(request)
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getTriggerId())
                .toUri();

        return ResponseEntity.created(location)
                .body(TriggerRuleMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TriggerRuleResponseDTO> updateTriggerRule(
            @PathVariable String id,
            @RequestBody TriggerRuleRequestDTO request) {

        TriggerRule existing = triggerRuleService.getTriggerRuleById(id);

        existing.setTriggerType(Enum.valueOf(TriggerType.class, request.getTriggerType()));
        existing.setConditionExpression(request.getConditionExpression());
        existing.setEnabled(request.isEnabled());

        TriggerRule updated = triggerRuleService.saveTriggerRule(existing);

        return ResponseEntity.ok(
                TriggerRuleMapper.toDTO(updated)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTriggerRule(@PathVariable String id) {
        triggerRuleService.deleteTriggerRuleById(id);
        return ResponseEntity.noContent().build();
    }
}