package org.chamsoft.smarthome.deviceregistry.presentationlayer;

import org.chamsoft.smarthome.deviceregistry.dataccesslayer.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.sql.init.mode=never"
)
@AutoConfigureWebTestClient
class DeviceControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();
    }

    @Test
    void createAndGetDevice_whenRequestIsValid_returnsCreatedDevice() {
        DeviceResponseDTO created = createDevice("Hallway Sensor");

        webTestClient.get()
                .uri("/api/v1/devices/{id}", created.getDeviceId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(DeviceResponseDTO.class)
                .value(response -> {
                    assertThat(response.getDeviceName()).isEqualTo("Hallway Sensor");
                    assertThat(response.getStatus()).isEqualTo("OFFLINE");
                    assertThat(response.getCapabilities()).containsExactlyInAnyOrder("MOTION_DETECTION");
                });
    }

    @Test
    void getDevices_whenDevicesExist_returnsCollection() {
        createDevice("First Sensor");
        createDevice("Second Sensor");

        webTestClient.get()
                .uri("/api/v1/devices")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DeviceResponseDTO.class)
                .value(response -> assertThat(response).hasSize(2));
    }

    @Test
    void updateDevice_whenDeviceExists_returnsUpdatedDevice() {
        DeviceResponseDTO created = createDevice("Old Camera");

        webTestClient.put()
                .uri("/api/v1/devices/{id}", created.getDeviceId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deviceRequest("Front Door Camera"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(DeviceResponseDTO.class)
                .value(response -> assertThat(response.getDeviceName()).isEqualTo("Front Door Camera"));
    }

    @Test
    void deleteDevice_whenDeviceExists_returnsNoContent() {
        DeviceResponseDTO created = createDevice("Delete Me");

        webTestClient.delete()
                .uri("/api/v1/devices/{id}", created.getDeviceId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(deviceRepository.findById(created.getDeviceId())).isEmpty();
    }

    @Test
    void getDeviceById_whenDeviceDoesNotExist_returnsNotFound() {
        webTestClient.get()
                .uri("/api/v1/devices/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").value(message -> assertThat(message.toString()).contains("Device not found"));
    }

    @Test
    void createDevice_whenDeviceNameAlreadyExists_returnsConflict() {
        createDevice("Duplicate Sensor");

        webTestClient.post()
                .uri("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deviceRequest("Duplicate Sensor"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.message").isEqualTo("Device name already exists: Duplicate Sensor");
    }

    private DeviceResponseDTO createDevice(String deviceName) {
        return webTestClient.post()
                .uri("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(deviceRequest(deviceName))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DeviceResponseDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private Map<String, Object> deviceRequest(String deviceName) {
        return Map.of(
                "deviceName", deviceName,
                "deviceType", "SENSOR",
                "firmwareVersion", "1.0.0",
                "capabilities", List.of("MOTION_DETECTION"),
                "connectivityType", "WIFI"
        );
    }
}
