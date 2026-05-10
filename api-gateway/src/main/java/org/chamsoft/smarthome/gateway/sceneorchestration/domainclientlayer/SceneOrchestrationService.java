package org.chamsoft.smarthome.gateway.sceneorchestration.domainclientlayer;

import org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer.SceneOrchestrationRequestDTO;
import org.chamsoft.smarthome.gateway.sceneorchestration.presentationlayer.SceneOrchestrationResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class SceneOrchestrationService {

    private final RestTemplate restTemplate;

    @Value("${microservices.scene-orchestrator.base-url}")
    private String baseUrl;

    public SceneOrchestrationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<SceneOrchestrationResponseDTO> getAllSceneOrchestrations() {
        ResponseEntity<SceneOrchestrationResponseDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/api/v1/scene-orchestrations",
                SceneOrchestrationResponseDTO[].class
        );

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public SceneOrchestrationResponseDTO getSceneOrchestrationById(UUID orchestrationId) {
        return restTemplate.getForObject(
                baseUrl + "/api/v1/scene-orchestrations/" + orchestrationId,
                SceneOrchestrationResponseDTO.class
        );
    }

    public SceneOrchestrationResponseDTO createSceneOrchestration(SceneOrchestrationRequestDTO requestDTO) {
        return restTemplate.postForObject(
                baseUrl + "/api/v1/scene-orchestrations",
                requestDTO,
                SceneOrchestrationResponseDTO.class
        );
    }

    public SceneOrchestrationResponseDTO updateSceneOrchestration(UUID orchestrationId,
                                                                  SceneOrchestrationRequestDTO requestDTO) {
        ResponseEntity<SceneOrchestrationResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/scene-orchestrations/" + orchestrationId,
                HttpMethod.PUT,
                new HttpEntity<>(requestDTO),
                SceneOrchestrationResponseDTO.class
        );
        return response.getBody();
    }

    public void deleteSceneOrchestration(UUID orchestrationId) {
        restTemplate.delete(baseUrl + "/api/v1/scene-orchestrations/" + orchestrationId);
    }
}
