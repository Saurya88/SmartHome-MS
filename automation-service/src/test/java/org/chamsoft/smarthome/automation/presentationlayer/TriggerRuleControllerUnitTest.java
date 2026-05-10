package org.chamsoft.smarthome.automation.presentationlayer;

import org.chamsoft.smarthome.automation.businesslogiclayer.TriggerRuleService;
import org.chamsoft.smarthome.automation.domain.TriggerRule;
import org.chamsoft.smarthome.automation.domain.TriggerType;
import org.chamsoft.smarthome.automation.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(TriggerRuleController.class)
class TriggerRuleControllerUnitTest {

    private WebTestClient webTestClient;

    @MockBean
    private TriggerRuleService triggerRuleService;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Test
    void createTriggerRule_whenServiceReturnsRule_returnsCreated() {
        TriggerRule triggerRule = triggerRule("night-trigger");
        when(triggerRuleService.saveTriggerRule(any())).thenReturn(triggerRule);

        webTestClient.post()
                .uri("/api/v1/trigger-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(triggerRuleRequest("night-trigger"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("Location", ".*/api/v1/trigger-rules/night-trigger")
                .expectBody()
                .jsonPath("$.triggerId").isEqualTo("night-trigger");
    }

    @Test
    void getTriggerRule_whenServiceReturnsRule_returnsOk() {
        when(triggerRuleService.getTriggerRuleById("night-trigger")).thenReturn(triggerRule("night-trigger"));

        webTestClient.get()
                .uri("/api/v1/trigger-rules/{id}", "night-trigger")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.triggerId").isEqualTo("night-trigger");
    }

    @Test
    void getTriggerRule_whenServiceThrowsNotFound_returnsNotFound() {
        when(triggerRuleService.getTriggerRuleById("missing-trigger"))
                .thenThrow(new ResourceNotFoundException("TriggerRule not found: missing-trigger"));

        webTestClient.get()
                .uri("/api/v1/trigger-rules/{id}", "missing-trigger")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("TriggerRule not found: missing-trigger");
    }

    private TriggerRule triggerRule(String triggerId) {
        return TriggerRule.builder()
                .triggerId(triggerId)
                .triggerType(TriggerType.TIME_BASED)
                .conditionExpression("time == 22:00")
                .enabled(true)
                .build();
    }

    private Map<String, Object> triggerRuleRequest(String triggerId) {
        return Map.of(
                "triggerId", triggerId,
                "triggerType", "TIME_BASED",
                "conditionExpression", "time == 22:00",
                "enabled", true
        );
    }
}
