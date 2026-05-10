package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class DeviceServiceClient {

    private final RestTemplate restTemplate;

    @Value("${microservices.device.base-url}")
    private String baseUrl;

    public DeviceServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DeviceClientResponseDTO getDeviceById(UUID deviceId) {
        return restTemplate.getForObject(
                baseUrl + "/api/v1/devices/" + deviceId,
                DeviceClientResponseDTO.class
        );
    }
}
