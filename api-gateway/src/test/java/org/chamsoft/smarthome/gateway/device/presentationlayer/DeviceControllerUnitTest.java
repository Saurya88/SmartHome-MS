package org.chamsoft.smarthome.gateway.device.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chamsoft.smarthome.gateway.device.domainclientlayer.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(DeviceController.class)
class DeviceControllerUnitTest {

    private WebTestClient webTestClient;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceService deviceService;

    @Test
    void getAllDevices_whenServiceReturnsData_returnsOk() throws Exception {
        DeviceResponseDTO dto = DeviceResponseDTO.builder()
                .deviceId(UUID.randomUUID())
                .deviceName("Front Door Lock")
                .deviceType("LOCK")
                .status("ONLINE")
                .firmwareVersion("1.2.3")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();

        when(deviceService.getAllDevices()).thenReturn(List.of(dto));

        webTestClient.get()
                .uri("/api/v1/devices")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.deviceResponseDTOList[0].deviceName").isEqualTo("Front Door Lock")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void getDeviceById_whenServiceReturnsData_returnsOk() throws Exception {
        UUID deviceId = UUID.randomUUID();
        DeviceResponseDTO dto = DeviceResponseDTO.builder()
                .deviceId(deviceId)
                .deviceName("Front Door Lock")
                .deviceType("LOCK")
                .status("ONLINE")
                .firmwareVersion("1.2.3")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();

        when(deviceService.getDeviceById(deviceId)).thenReturn(dto);

        webTestClient.get()
                .uri("/api/v1/devices/{id}", deviceId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.deviceId").isEqualTo(deviceId.toString())
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void createDevice_whenServiceSucceeds_returnsCreated() throws Exception {
        UUID deviceId = UUID.randomUUID();
        DeviceRequestDTO requestDTO = DeviceRequestDTO.builder()
                .deviceName("Front Door Lock")
                .deviceType("LOCK")
                .firmwareVersion("1.2.3")
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();

        DeviceResponseDTO dto = DeviceResponseDTO.builder()
                .deviceId(deviceId)
                .deviceName("Front Door Lock")
                .deviceType("LOCK")
                .status("ONLINE")
                .firmwareVersion("1.2.3")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();

        when(deviceService.createDevice(any())).thenReturn(dto);

        webTestClient.post()
                .uri("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.deviceId").isEqualTo(deviceId.toString());
    }

    @Test
    void updateDevice_whenServiceSucceeds_returnsOk() throws Exception {
        UUID deviceId = UUID.randomUUID();
        DeviceRequestDTO requestDTO = DeviceRequestDTO.builder()
                .deviceName("Updated")
                .deviceType("LOCK")
                .firmwareVersion("1.2.4")
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();

        DeviceResponseDTO dto = DeviceResponseDTO.builder()
                .deviceId(deviceId)
                .deviceName("Updated")
                .deviceType("LOCK")
                .status("ONLINE")
                .firmwareVersion("1.2.4")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("LOCK"))
                .connectivityType("ZIGBEE")
                .build();

        when(deviceService.updateDevice(eq(deviceId), any())).thenReturn(dto);

        webTestClient.put()
                .uri("/api/v1/devices/{id}", deviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.deviceName").isEqualTo("Updated");
    }

    @Test
    void deleteDevice_whenServiceSucceeds_returnsNoContent() throws Exception {
        UUID deviceId = UUID.randomUUID();
        doNothing().when(deviceService).deleteDevice(deviceId);

        webTestClient.delete()
                .uri("/api/v1/devices/{id}", deviceId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getDeviceById_whenDownstreamReturnsNotFound_returnsNotFound() throws Exception {
        UUID deviceId = UUID.randomUUID();
        when(deviceService.getDeviceById(deviceId)).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "{\"message\":\"Device not found\"}".getBytes(),
                null
        ));

        webTestClient.get()
                .uri("/api/v1/devices/{id}", deviceId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Device not found");
    }
}
