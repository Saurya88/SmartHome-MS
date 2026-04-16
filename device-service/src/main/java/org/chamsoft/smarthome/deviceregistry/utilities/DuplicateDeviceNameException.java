package org.chamsoft.smarthome.deviceregistry.utilities;

public class DuplicateDeviceNameException extends BusinessRuleViolationException {
    public DuplicateDeviceNameException(String deviceName) {
        super("Device name already exists: " + deviceName);
    }
}
