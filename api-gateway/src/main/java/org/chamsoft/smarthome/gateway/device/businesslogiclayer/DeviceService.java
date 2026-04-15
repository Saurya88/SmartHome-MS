package org.chamsoft.smarthome.gateway.device.businesslogiclayer;

import org.chamsoft.smarthome.gateway.device.presentationlayer.DeviceRequestDTO;
import org.chamsoft.smarthome.gateway.device.presentationlayer.DeviceResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DeviceService {

    private final RestTemplate restTemplate;

    @Value("${microservices.device.base-url}")
    private String deviceServiceBaseUrl;

    public DeviceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<DeviceResponseDTO> getAllDevices() {
        ResponseEntity<DeviceResponseDTO[]> response =
                restTemplate.getForEntity(deviceServiceBaseUrl + "/api/v1/devices", DeviceResponseDTO[].class);

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public DeviceResponseDTO getDeviceById(UUID deviceId) {
        return restTemplate.getForObject(
                deviceServiceBaseUrl + "/api/v1/devices/" + deviceId,
                DeviceResponseDTO.class
        );
    }

    public DeviceResponseDTO createDevice(DeviceRequestDTO requestDTO) {
        return restTemplate.postForObject(
                deviceServiceBaseUrl + "/api/v1/devices",
                requestDTO,
                DeviceResponseDTO.class
        );
    }

    public DeviceResponseDTO updateDevice(UUID deviceId, DeviceRequestDTO requestDTO) {
        restTemplate.put(deviceServiceBaseUrl + "/api/v1/devices/" + deviceId, requestDTO);
        return getDeviceById(deviceId);
    }

    public void deleteDevice(UUID deviceId) {
        restTemplate.delete(deviceServiceBaseUrl + "/api/v1/devices/" + deviceId);
    }
}
