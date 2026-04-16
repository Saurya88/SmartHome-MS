package org.chamsoft.smarthome.deviceregistry.datamappinglayer;

import org.chamsoft.smarthome.deviceregistry.domain.Device;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceStatus;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceType;
import org.chamsoft.smarthome.deviceregistry.presentationlayer.DeviceRequestDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceMapperTest {

    @Test
    void toEntity_whenDtoIsNull_returnsNull() {
        assertThat(DeviceMapper.toEntity(null)).isNull();
    }

    @Test
    void toDTO_whenDeviceIsNull_returnsNull() {
        assertThat(DeviceMapper.toDTO(null)).isNull();
    }

    @Test
    void toEntity_whenDtoIsValid_mapsDefaultStatusAndFields() {
        DeviceRequestDTO dto = DeviceRequestDTO.builder()
                .deviceName("Office Thermostat")
                .deviceType("THERMOSTAT")
                .firmwareVersion("2.0.0")
                .capabilities(List.of("TEMPERATURE_CONTROL"))
                .connectivityType("WIFI")
                .build();

        Device device = DeviceMapper.toEntity(dto);

        assertThat(device.getDeviceName()).isEqualTo("Office Thermostat");
        assertThat(device.getDeviceType()).isEqualTo(DeviceType.THERMOSTAT);
        assertThat(device.getStatus()).isEqualTo(DeviceStatus.OFFLINE);
    }

    @Test
    void toDTO_whenDeviceIsValid_mapsFields() {
        Device device = Device.builder()
                .deviceName("Office Thermostat")
                .deviceType(DeviceType.THERMOSTAT)
                .status(DeviceStatus.ONLINE)
                .firmwareVersion("2.0.0")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("TEMPERATURE_CONTROL"))
                .connectivityType("WIFI")
                .build();

        var dto = DeviceMapper.toDTO(device);

        assertThat(dto.getDeviceName()).isEqualTo("Office Thermostat");
        assertThat(dto.getDeviceType()).isEqualTo("THERMOSTAT");
        assertThat(dto.getStatus()).isEqualTo("ONLINE");
    }
}
