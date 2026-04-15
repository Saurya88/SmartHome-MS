package org.chamsoft.smarthome.automation.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Condition {

    private String attribute;
    private String operator;
    private String conditionValue;

    public Condition() {}

    public Condition(String attribute, String operator, String conditionValue) {
        this.attribute = attribute;
        this.operator = operator;
        this.conditionValue = conditionValue;
    }

    public String getAttribute() { return attribute; }
    public String getOperator() { return operator; }
    public String getConditionValue() { return conditionValue; }
}