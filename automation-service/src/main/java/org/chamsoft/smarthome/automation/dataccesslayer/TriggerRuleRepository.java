package org.chamsoft.smarthome.automation.dataccesslayer;

import org.chamsoft.smarthome.automation.domain.TriggerRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TriggerRuleRepository extends JpaRepository<TriggerRule, String> {
}