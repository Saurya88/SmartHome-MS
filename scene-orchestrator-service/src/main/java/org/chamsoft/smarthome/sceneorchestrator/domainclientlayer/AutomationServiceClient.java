package org.chamsoft.smarthome.sceneorchestrator.domainclientlayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class AutomationServiceClient {

    private final RestTemplate restTemplate;

    @Value("${microservices.automation.base-url}")
    private String baseUrl;

    public AutomationServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AutomationSceneResponseDTO createScene(AutomationSceneRequestDTO requestDTO) {
        return restTemplate.postForObject(
                baseUrl + "/api/v1/scenes",
                requestDTO,
                AutomationSceneResponseDTO.class
        );
    }

    public AutomationSceneResponseDTO updateScene(UUID sceneId, AutomationSceneRequestDTO requestDTO) {
        ResponseEntity<AutomationSceneResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/scenes/" + sceneId,
                HttpMethod.PUT,
                new HttpEntity<>(requestDTO),
                AutomationSceneResponseDTO.class
        );
        return response.getBody();
    }

    public AutomationSceneResponseDTO addDeviceActions(UUID sceneId, List<AutomationDeviceActionRequestDTO> actions) {
        ResponseEntity<AutomationSceneResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/scenes/" + sceneId + "/device-actions",
                HttpMethod.POST,
                new HttpEntity<>(actions),
                AutomationSceneResponseDTO.class
        );
        return response.getBody();
    }

    public AutomationSceneResponseDTO addTriggerRules(UUID sceneId, List<AutomationTriggerRuleRequestDTO> rules) {
        ResponseEntity<AutomationSceneResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/scenes/" + sceneId + "/trigger-rules",
                HttpMethod.POST,
                new HttpEntity<>(rules),
                AutomationSceneResponseDTO.class
        );
        return response.getBody();
    }

    public AutomationSceneResponseDTO replaceDeviceActions(UUID sceneId, List<AutomationDeviceActionRequestDTO> actions) {
        ResponseEntity<AutomationSceneResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/scenes/" + sceneId + "/device-actions",
                HttpMethod.PUT,
                new HttpEntity<>(actions),
                AutomationSceneResponseDTO.class
        );
        return response.getBody();
    }

    public AutomationSceneResponseDTO replaceTriggerRules(UUID sceneId, List<AutomationTriggerRuleRequestDTO> rules) {
        ResponseEntity<AutomationSceneResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/scenes/" + sceneId + "/trigger-rules",
                HttpMethod.PUT,
                new HttpEntity<>(rules),
                AutomationSceneResponseDTO.class
        );
        return response.getBody();
    }

    public AutomationSceneResponseDTO activateScene(UUID sceneId) {
        ResponseEntity<AutomationSceneResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/scenes/" + sceneId + "/activate",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                AutomationSceneResponseDTO.class
        );
        return response.getBody();
    }

    public void deleteScene(UUID sceneId) {
        restTemplate.delete(baseUrl + "/api/v1/scenes/" + sceneId);
    }
}
