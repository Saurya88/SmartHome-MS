package org.chamsoft.smarthome.automation.businesslogiclayer;

import org.chamsoft.smarthome.automation.dataccesslayer.TriggerRuleRepository;
import org.chamsoft.smarthome.automation.domain.TriggerRule;
import org.chamsoft.smarthome.automation.utilities.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TriggerRuleService {

    private final TriggerRuleRepository triggerRuleRepository;

    public TriggerRuleService(TriggerRuleRepository triggerRuleRepository) {
        this.triggerRuleRepository = triggerRuleRepository;
    }

    public List<TriggerRule> getAllTriggerRules() {
        return triggerRuleRepository.findAll();
    }

    public TriggerRule getTriggerRuleById(String id) {
        return triggerRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TriggerRule not found: " + id));
    }

    public TriggerRule saveTriggerRule(TriggerRule triggerRule) {
        return triggerRuleRepository.save(triggerRule);
    }

    public void deleteTriggerRuleById(String id) {
        triggerRuleRepository.deleteById(id);
    }
}