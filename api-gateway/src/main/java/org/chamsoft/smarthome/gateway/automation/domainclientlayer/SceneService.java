package org.chamsoft.smarthome.gateway.automation.domainclientlayer;

import org.chamsoft.smarthome.gateway.automation.presentationlayer.DeviceActionRequestDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.SceneRequestDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.SceneResponseDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.TriggerRuleRequestDTO;
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
public class SceneService {

    private final RestTemplate restTemplate;

    @Value("${microservices.automation.base-url}")
    private String baseUrl;

    public SceneService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<SceneResponseDTO> getAllScenes() {
        ResponseEntity<SceneResponseDTO[]> response =
                restTemplate.getForEntity(baseUrl + "/api/v1/scenes", SceneResponseDTO[].class);

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public SceneResponseDTO getSceneById(UUID sceneId) {
        return restTemplate.getForObject(
                baseUrl + "/api/v1/scenes/" + sceneId,
                SceneResponseDTO.class
        );
    }

    public SceneResponseDTO createScene(SceneRequestDTO requestDTO) {
        return restTemplate.postForObject(
                baseUrl + "/api/v1/scenes",
                requestDTO,
                SceneResponseDTO.class
        );
    }

    public SceneResponseDTO updateScene(UUID sceneId, SceneRequestDTO requestDTO) {
        restTemplate.put(baseUrl + "/api/v1/scenes/" + sceneId, requestDTO);
        return getSceneById(sceneId);
    }

    public SceneResponseDTO addDeviceActions(UUID sceneId, List<DeviceActionRequestDTO> actions) {
        return restTemplate.postForObject(
                baseUrl + "/api/v1/scenes/" + sceneId + "/device-actions",
                actions,
                SceneResponseDTO.class
        );
    }

    public SceneResponseDTO replaceDeviceActions(UUID sceneId, List<DeviceActionRequestDTO> actions) {
        ResponseEntity<SceneResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/scenes/" + sceneId + "/device-actions",
                HttpMethod.PUT,
                new HttpEntity<>(actions),
                SceneResponseDTO.class
        );
        return response.getBody();
    }

    public SceneResponseDTO addTriggerRules(UUID sceneId, List<TriggerRuleRequestDTO> rules) {
        return restTemplate.postForObject(
                baseUrl + "/api/v1/scenes/" + sceneId + "/trigger-rules",
                rules,
                SceneResponseDTO.class
        );
    }

    public SceneResponseDTO replaceTriggerRules(UUID sceneId, List<TriggerRuleRequestDTO> rules) {
        ResponseEntity<SceneResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/scenes/" + sceneId + "/trigger-rules",
                HttpMethod.PUT,
                new HttpEntity<>(rules),
                SceneResponseDTO.class
        );
        return response.getBody();
    }

    public SceneResponseDTO activateScene(UUID sceneId) {
        ResponseEntity<SceneResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/scenes/" + sceneId + "/activate",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                SceneResponseDTO.class
        );
        return response.getBody();
    }

    public void deleteScene(UUID sceneId) {
        restTemplate.delete(baseUrl + "/api/v1/scenes/" + sceneId);
    }
}
