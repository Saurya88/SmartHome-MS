package org.chamsoft.smarthome.deviceregistry.utilities;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
