package org.chamsoft.smarthome.gateway.automation.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chamsoft.smarthome.gateway.automation.domainclientlayer.TriggerRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(TriggerRuleController.class)
class TriggerRuleControllerUnitTest {

    private WebTestClient webTestClient;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TriggerRuleService triggerRuleService;

    @Test
    void getAllTriggerRules_whenServiceReturnsData_returnsOk() throws Exception {
        TriggerRuleResponseDTO dto = TriggerRuleResponseDTO.builder()
                .triggerId("rule-1")
                .triggerType("TIME_BASED")
                .conditionExpression("time == 22:00")
                .enabled(true)
                .value("22:00")
                .build();

        when(triggerRuleService.getAllTriggerRules()).thenReturn(List.of(dto));

        webTestClient.get()
                .uri("/api/v1/trigger-rules")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.triggerRuleResponseDTOList[0].triggerId").isEqualTo("rule-1")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void getTriggerRuleById_whenServiceReturnsData_returnsOk() throws Exception {
        TriggerRuleResponseDTO dto = TriggerRuleResponseDTO.builder()
                .triggerId("rule-1")
                .triggerType("TIME_BASED")
                .conditionExpression("time == 22:00")
                .enabled(true)
                .value("22:00")
                .build();

        when(triggerRuleService.getTriggerRuleById("rule-1")).thenReturn(dto);

        webTestClient.get()
                .uri("/api/v1/trigger-rules/{id}", "rule-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.triggerId").isEqualTo("rule-1")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void createTriggerRule_whenServiceSucceeds_returnsCreated() throws Exception {
        TriggerRuleRequestDTO requestDTO = TriggerRuleRequestDTO.builder()
                .triggerId("rule-1")
                .triggerType("TIME_BASED")
                .conditionExpression("time == 22:00")
                .enabled(true)
                .build();

        TriggerRuleResponseDTO dto = TriggerRuleResponseDTO.builder()
                .triggerId("rule-1")
                .triggerType("TIME_BASED")
                .conditionExpression("time == 22:00")
                .enabled(true)
                .value("22:00")
                .build();

        when(triggerRuleService.createTriggerRule(any())).thenReturn(dto);

        webTestClient.post()
                .uri("/api/v1/trigger-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.triggerId").isEqualTo("rule-1");
    }

    @Test
    void updateTriggerRule_whenServiceSucceeds_returnsOk() throws Exception {
        TriggerRuleRequestDTO requestDTO = TriggerRuleRequestDTO.builder()
                .triggerId("rule-1")
                .triggerType("MANUAL")
                .conditionExpression("manual")
                .enabled(false)
                .build();

        TriggerRuleResponseDTO dto = TriggerRuleResponseDTO.builder()
                .triggerId("rule-1")
                .triggerType("MANUAL")
                .conditionExpression("manual")
                .enabled(false)
                .value("manual")
                .build();

        when(triggerRuleService.updateTriggerRule(eq("rule-1"), any())).thenReturn(dto);

        webTestClient.put()
                .uri("/api/v1/trigger-rules/{id}", "rule-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.triggerType").isEqualTo("MANUAL");
    }

    @Test
    void deleteTriggerRule_whenServiceSucceeds_returnsNoContent() throws Exception {
        doNothing().when(triggerRuleService).deleteTriggerRule("rule-1");

        webTestClient.delete()
                .uri("/api/v1/trigger-rules/{id}", "rule-1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getTriggerRuleById_whenDownstreamReturnsNotFound_returnsNotFound() throws Exception {
        when(triggerRuleService.getTriggerRuleById("missing-rule")).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "{\"message\":\"TriggerRule not found: missing-rule\"}".getBytes(),
                null
        ));

        webTestClient.get()
                .uri("/api/v1/trigger-rules/{id}", "missing-rule")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("TriggerRule not found: missing-rule");
    }
}
