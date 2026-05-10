package org.chamsoft.smarthome.deviceregistry.businesslogiclayer;

import org.chamsoft.smarthome.deviceregistry.dataccesslayer.DeviceRepository;
import org.chamsoft.smarthome.deviceregistry.domain.Device;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceStatus;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceType;
import org.chamsoft.smarthome.deviceregistry.utilities.DuplicateDeviceNameException;
import org.chamsoft.smarthome.deviceregistry.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceUnitTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    @Test
    void createDevice_whenNameIsUnique_savesDevice() {
        Device device = device("Hallway Sensor");
        when(deviceRepository.existsByDeviceName("Hallway Sensor")).thenReturn(false);
        when(deviceRepository.save(device)).thenReturn(device);

        Device result = deviceService.createDevice(device);

        assertThat(result.getDeviceName()).isEqualTo("Hallway Sensor");
        verify(deviceRepository).save(device);
    }

    @Test
    void createDevice_whenNameAlreadyExists_throwsDuplicateDeviceNameException() {
        Device device = device("Hallway Sensor");
        when(deviceRepository.existsByDeviceName("Hallway Sensor")).thenReturn(true);

        assertThatThrownBy(() -> deviceService.createDevice(device))
                .isInstanceOf(DuplicateDeviceNameException.class)
                .hasMessage("Device name already exists: Hallway Sensor");
    }

    @Test
    void getDeviceById_whenMissing_throwsResourceNotFoundException() {
        UUID deviceId = UUID.randomUUID();
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getDeviceById(deviceId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(deviceId.toString());
    }

    @Test
    void updateDevice_whenDeviceExists_updatesMutableFields() {
        UUID deviceId = UUID.randomUUID();
        Device existing = device("Old Name");
        existing.setDeviceId(deviceId);
        Device update = device("New Name");
        update.setDeviceType(DeviceType.LOCK);
        update.setCapabilities(List.of("LOCK"));

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(existing)).thenReturn(existing);

        Device result = deviceService.updateDevice(deviceId, update);

        assertThat(result.getDeviceName()).isEqualTo("New Name");
        assertThat(result.getDeviceType()).isEqualTo(DeviceType.LOCK);
        assertThat(result.getCapabilities()).containsExactly("LOCK");
    }

    private Device device(String name) {
        return Device.builder()
                .deviceId(UUID.randomUUID())
                .deviceName(name)
                .deviceType(DeviceType.SENSOR)
                .status(DeviceStatus.ONLINE)
                .firmwareVersion("1.0.0")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("MOTION_DETECTION"))
                .connectivityType("WIFI")
                .build();
    }
}
