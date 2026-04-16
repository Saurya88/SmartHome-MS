package org.chamsoft.smarthome.gateway.device.presentationlayer;

import org.chamsoft.smarthome.gateway.device.domainclientlayer.DeviceService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<DeviceResponseDTO>>> getAllDevices() {
        List<EntityModel<DeviceResponseDTO>> devices = deviceService.getAllDevices().stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(
                devices,
                linkTo(methodOn(DeviceController.class).getAllDevices()).withSelfRel()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<DeviceResponseDTO>> getDeviceById(@PathVariable UUID id) {
        return ResponseEntity.ok(toModel(deviceService.getDeviceById(id)));
    }

    @PostMapping
    public ResponseEntity<EntityModel<DeviceResponseDTO>> createDevice(@RequestBody DeviceRequestDTO requestDTO) {
        DeviceResponseDTO created = deviceService.createDevice(requestDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getDeviceId())
                .toUri();

        return ResponseEntity.created(location).body(toModel(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<DeviceResponseDTO>> updateDevice(@PathVariable UUID id,
                                                                       @RequestBody DeviceRequestDTO requestDTO) {
        return ResponseEntity.ok(toModel(deviceService.updateDevice(id, requestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<DeviceResponseDTO> toModel(DeviceResponseDTO device) {
        return EntityModel.of(
                device,
                linkTo(methodOn(DeviceController.class).getDeviceById(device.getDeviceId())).withSelfRel(),
                linkTo(methodOn(DeviceController.class).getAllDevices()).withRel("devices")
        );
    }
}
