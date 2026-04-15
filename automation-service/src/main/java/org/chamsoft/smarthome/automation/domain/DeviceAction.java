package org.chamsoft.smarthome.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Embeddable
public class DeviceAction {

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "device_id", columnDefinition = "VARCHAR(36)")
    private UUID deviceId;
    private String actionType;
    private String parameters;

    public DeviceAction() {}

    public DeviceAction(UUID deviceId, String actionType, String parameters) {
        this.deviceId = deviceId;
        this.actionType = actionType;
        this.parameters = parameters;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getParameters() {
        return parameters;
    }
}
