package org.chamsoft.smarthome.deviceregistry.presentationlayer;

import jakarta.validation.Valid;
import org.chamsoft.smarthome.deviceregistry.businesslogiclayer.DeviceService;
import org.chamsoft.smarthome.deviceregistry.datamappinglayer.DeviceMapper;
import org.chamsoft.smarthome.deviceregistry.domain.Device;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponseDTO>> getDevices() {
        List<DeviceResponseDTO> list = deviceService.getAllDevices()
                .stream()
                .map(DeviceMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> getDeviceById(@PathVariable UUID id) {
        Device device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(DeviceMapper.toDTO(device));
    }

    @PostMapping
    public ResponseEntity<DeviceResponseDTO> createDevice(@Valid @RequestBody DeviceRequestDTO request) {
        Device device = DeviceMapper.toEntity(request);
        Device saved = deviceService.createDevice(device);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getDeviceId())
                .toUri();

        return ResponseEntity.created(location)
                .body(DeviceMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponseDTO> updateDevice(@PathVariable UUID id,
                                                          @Valid @RequestBody DeviceRequestDTO request) {
        Device updated = DeviceMapper.toEntity(request);
        Device saved = deviceService.updateDevice(id, updated);

        return ResponseEntity.ok(DeviceMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
