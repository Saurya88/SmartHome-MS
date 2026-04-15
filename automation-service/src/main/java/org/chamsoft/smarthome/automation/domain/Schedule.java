package org.chamsoft.smarthome.automation.domain;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class Schedule {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String recurrencePattern;

    public Schedule() {}

    public Schedule(LocalDateTime startTime, LocalDateTime endTime, String recurrencePattern) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.recurrencePattern = recurrencePattern;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getRecurrencePattern() {
        return recurrencePattern;
    }
}
