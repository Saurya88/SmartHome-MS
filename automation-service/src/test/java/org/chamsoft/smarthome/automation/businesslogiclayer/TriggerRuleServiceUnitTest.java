package org.chamsoft.smarthome.automation.businesslogiclayer;

import org.chamsoft.smarthome.automation.dataccesslayer.TriggerRuleRepository;
import org.chamsoft.smarthome.automation.domain.TriggerRule;
import org.chamsoft.smarthome.automation.domain.TriggerType;
import org.chamsoft.smarthome.automation.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriggerRuleServiceUnitTest {

    @Mock
    private TriggerRuleRepository triggerRuleRepository;

    @InjectMocks
    private TriggerRuleService triggerRuleService;

    @Test
    void getTriggerRuleById_whenRuleExists_returnsRule() {
        TriggerRule triggerRule = triggerRule("morning-trigger");
        when(triggerRuleRepository.findById("morning-trigger")).thenReturn(Optional.of(triggerRule));

        TriggerRule result = triggerRuleService.getTriggerRuleById("morning-trigger");

        assertThat(result.getTriggerId()).isEqualTo("morning-trigger");
    }

    @Test
    void getTriggerRuleById_whenMissing_throwsResourceNotFoundException() {
        when(triggerRuleRepository.findById("missing-trigger")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> triggerRuleService.getTriggerRuleById("missing-trigger"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("TriggerRule not found: missing-trigger");
    }

    @Test
    void saveTriggerRule_persistsRule() {
        TriggerRule triggerRule = triggerRule("night-trigger");
        when(triggerRuleRepository.save(triggerRule)).thenReturn(triggerRule);

        TriggerRule result = triggerRuleService.saveTriggerRule(triggerRule);

        assertThat(result.getTriggerId()).isEqualTo("night-trigger");
        verify(triggerRuleRepository).save(triggerRule);
    }

    @Test
    void getAllTriggerRules_returnsAllRules() {
        when(triggerRuleRepository.findAll()).thenReturn(List.of(triggerRule("one"), triggerRule("two")));

        List<TriggerRule> result = triggerRuleService.getAllTriggerRules();

        assertThat(result).hasSize(2);
    }

    private TriggerRule triggerRule(String triggerId) {
        return TriggerRule.builder()
                .triggerId(triggerId)
                .triggerType(TriggerType.TIME_BASED)
                .conditionExpression("time == 22:00")
                .enabled(true)
                .triggerValue("22:00")
                .build();
    }
}
