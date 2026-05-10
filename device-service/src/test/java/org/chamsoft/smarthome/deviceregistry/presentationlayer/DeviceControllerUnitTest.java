package org.chamsoft.smarthome.deviceregistry.presentationlayer;

import org.chamsoft.smarthome.deviceregistry.businesslogiclayer.DeviceService;
import org.chamsoft.smarthome.deviceregistry.domain.Device;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceStatus;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceType;
import org.chamsoft.smarthome.deviceregistry.utilities.DuplicateDeviceNameException;
import org.chamsoft.smarthome.deviceregistry.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(DeviceController.class)
class DeviceControllerUnitTest {

    private WebTestClient webTestClient;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Test
    void getDeviceById_whenServiceReturnsDevice_returnsOk() {
        UUID deviceId = UUID.randomUUID();
        when(deviceService.getDeviceById(deviceId)).thenReturn(device(deviceId, "Front Door Lock"));

        webTestClient.get()
                .uri("/api/v1/devices/{id}", deviceId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.deviceId").isEqualTo(deviceId.toString())
                .jsonPath("$.deviceName").isEqualTo("Front Door Lock");
    }

    @Test
    void createDevice_whenServiceReturnsDevice_returnsCreated() {
        UUID deviceId = UUID.randomUUID();
        when(deviceService.createDevice(any())).thenReturn(device(deviceId, "Front Door Lock"));

        webTestClient.post()
                .uri("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deviceRequest("Front Door Lock"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("Location", ".*/api/v1/devices/" + deviceId)
                .expectBody()
                .jsonPath("$.deviceName").isEqualTo("Front Door Lock");
    }

    @Test
    void getDeviceById_whenServiceThrowsNotFound_returnsNotFound() {
        UUID deviceId = UUID.randomUUID();
        when(deviceService.getDeviceById(deviceId)).thenThrow(new ResourceNotFoundException("Device not found"));

        webTestClient.get()
                .uri("/api/v1/devices/{id}", deviceId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Device not found");
    }

    @Test
    void createDevice_whenNameAlreadyExists_returnsConflict() {
        when(deviceService.createDevice(any())).thenThrow(new DuplicateDeviceNameException("Front Door Lock"));

        webTestClient.post()
                .uri("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deviceRequest("Front Door Lock"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Device name already exists: Front Door Lock");
    }

    @Test
    void deleteDevice_whenServiceThrowsNotFound_returnsNotFound() {
        UUID deviceId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Device not found")).when(deviceService).deleteDevice(deviceId);

        webTestClient.delete()
                .uri("/api/v1/devices/{id}", deviceId)
                .exchange()
                .expectStatus().isNotFound();
    }

    private Device device(UUID deviceId, String name) {
        return Device.builder()
                .deviceId(deviceId)
                .deviceName(name)
                .deviceType(DeviceType.LOCK)
                .status(DeviceStatus.ONLINE)
                .firmwareVersion("1.0.0")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("LOCK"))
                .connectivityType("WIFI")
                .build();
    }

    private Map<String, Object> deviceRequest(String deviceName) {
        return Map.of(
                "deviceName", deviceName,
                "deviceType", "LOCK",
                "firmwareVersion", "1.0.0",
                "capabilities", List.of("LOCK"),
                "connectivityType", "WIFI"
        );
    }
}
