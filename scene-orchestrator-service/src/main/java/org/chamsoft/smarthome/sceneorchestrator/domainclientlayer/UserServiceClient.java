package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${microservices.user.base-url}")
    private String baseUrl;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserClientResponseDTO getUserById(UUID userId) {
        return restTemplate.getForObject(
                baseUrl + "/api/v1/users/" + userId,
                UserClientResponseDTO.class
        );
    }
}

