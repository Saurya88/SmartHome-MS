package org.chamsoft.smarthome.userpermissions.utilities;

public class DuplicateUsernameException extends BusinessRuleViolationException {
    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
    }
}
