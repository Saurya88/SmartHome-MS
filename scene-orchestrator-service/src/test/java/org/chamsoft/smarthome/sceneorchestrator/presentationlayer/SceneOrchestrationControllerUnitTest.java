package org.chamsoft.smarthome.sceneorchestrator.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chamsoft.smarthome.sceneorchestrator.businesslogiclayer.SceneOrchestrationService;
import org.chamsoft.smarthome.sceneorchestrator.utilities.GlobalExceptionHandler;
import org.chamsoft.smarthome.sceneorchestrator.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SceneOrchestrationControllerUnitTest {

    @Mock
    private SceneOrchestrationService sceneOrchestrationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(new SceneOrchestrationController(sceneOrchestrationService))
                .setControllerAdvice(new GlobalExceptionHandler(objectMapper))
                .build();
    }

    @Test
    void getAllSceneOrchestrations_whenServiceReturnsData_returnsOk() throws Exception {
        when(sceneOrchestrationService.getAllSceneOrchestrations()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/scene-orchestrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Night Routine"));
    }

    @Test
    void createSceneOrchestration_whenServiceSucceeds_returnsCreated() throws Exception {
        SceneOrchestrationRequestDTO requestDTO = sampleRequest();
        when(sceneOrchestrationService.createSceneOrchestration(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/scene-orchestrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateSceneOrchestration_whenServiceSucceeds_returnsOk() throws Exception {
        UUID orchestrationId = UUID.randomUUID();
        when(sceneOrchestrationService.updateSceneOrchestration(eq(orchestrationId), any())).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/scene-orchestrations/{id}", orchestrationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.automationSceneId").exists());
    }

    @Test
    void deleteSceneOrchestration_whenServiceSucceeds_returnsNoContent() throws Exception {
        UUID orchestrationId = UUID.randomUUID();
        doNothing().when(sceneOrchestrationService).deleteSceneOrchestration(orchestrationId);

        mockMvc.perform(delete("/api/v1/scene-orchestrations/{id}", orchestrationId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSceneOrchestrationById_whenServiceThrowsNotFound_returns404() throws Exception {
        UUID orchestrationId = UUID.randomUUID();
        when(sceneOrchestrationService.getSceneOrchestrationById(orchestrationId))
                .thenThrow(new ResourceNotFoundException("Scene orchestration not found"));

        mockMvc.perform(get("/api/v1/scene-orchestrations/{id}", orchestrationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Scene orchestration not found"));
    }

    private SceneOrchestrationRequestDTO sampleRequest() {
        return SceneOrchestrationRequestDTO.builder()
                .ownerUserId(UUID.randomUUID())
                .name("Night Routine")
                .status("ACTIVE")
                .deviceActions(List.of(SceneOrchestrationRequestDTO.DeviceActionRequestDTO.builder()
                        .deviceId(UUID.randomUUID())
                        .actionType("LOCK")
                        .parameters("{}")
                        .build()))
                .triggerRules(List.of(SceneOrchestrationRequestDTO.TriggerRuleRequestDTO.builder()
                        .triggerId("night-trigger")
                        .triggerType("TIME_BASED")
                        .conditionExpression("time == 22:00")
                        .enabled(true)
                        .triggerValue("22:00")
                        .build()))
                .build();
    }

    private SceneOrchestrationResponseDTO sampleResponse() {
        return SceneOrchestrationResponseDTO.builder()
                .orchestrationId(UUID.randomUUID())
                .ownerUserId(UUID.randomUUID())
                .automationSceneId(UUID.randomUUID())
                .name("Night Routine")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .deviceActions(List.of(SceneOrchestrationResponseDTO.DeviceActionResponseDTO.builder()
                        .deviceId(UUID.randomUUID())
                        .actionType("LOCK")
                        .parameters("{}")
                        .build()))
                .triggerRules(List.of(SceneOrchestrationResponseDTO.TriggerRuleResponseDTO.builder()
                        .triggerId("night-trigger")
                        .triggerType("TIME_BASED")
                        .conditionExpression("time == 22:00")
                        .enabled(true)
                        .triggerValue("22:00")
                        .build()))
                .build();
    }
}
