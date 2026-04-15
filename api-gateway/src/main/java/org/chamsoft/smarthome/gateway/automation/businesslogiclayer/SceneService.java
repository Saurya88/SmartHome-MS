package org.chamsoft.smarthome.gateway.automation.businesslogiclayer;

import org.chamsoft.smarthome.gateway.automation.presentationlayer.SceneRequestDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.SceneResponseDTO;
import org.springframework.beans.factory.annotation.Value;
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

    public void deleteScene(UUID sceneId) {
        restTemplate.delete(baseUrl + "/api/v1/scenes/" + sceneId);
    }
}