package org.chamsoft.smarthome.gateway.automation.businesslogiclayer;

import org.chamsoft.smarthome.gateway.automation.presentationlayer.TriggerRuleRequestDTO;
import org.chamsoft.smarthome.gateway.automation.presentationlayer.TriggerRuleResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class TriggerRuleService {

    private final RestTemplate restTemplate;

    @Value("${microservices.automation.base-url}")
    private String baseUrl;

    public TriggerRuleService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<TriggerRuleResponseDTO> getAllTriggerRules() {
        ResponseEntity<TriggerRuleResponseDTO[]> response =
                restTemplate.getForEntity(baseUrl + "/api/v1/trigger-rules", TriggerRuleResponseDTO[].class);

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public TriggerRuleResponseDTO getTriggerRuleById(String triggerId) {
        return restTemplate.getForObject(
                baseUrl + "/api/v1/trigger-rules/" + triggerId,
                TriggerRuleResponseDTO.class
        );
    }

    public TriggerRuleResponseDTO createTriggerRule(TriggerRuleRequestDTO requestDTO) {
        return restTemplate.postForObject(
                baseUrl + "/api/v1/trigger-rules",
                requestDTO,
                TriggerRuleResponseDTO.class
        );
    }

    public TriggerRuleResponseDTO updateTriggerRule(String triggerId, TriggerRuleRequestDTO requestDTO) {
        HttpEntity<TriggerRuleRequestDTO> entity = new HttpEntity<>(requestDTO);

        ResponseEntity<TriggerRuleResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/trigger-rules/" + triggerId,
                HttpMethod.PUT,
                entity,
                TriggerRuleResponseDTO.class
        );

        return response.getBody();
    }

    public void deleteTriggerRule(String triggerId) {
        restTemplate.delete(baseUrl + "/api/v1/trigger-rules/" + triggerId);
    }
}