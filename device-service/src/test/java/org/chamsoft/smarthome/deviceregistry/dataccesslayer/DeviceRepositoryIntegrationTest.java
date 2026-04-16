package org.chamsoft.smarthome.deviceregistry.dataccesslayer;

import org.chamsoft.smarthome.deviceregistry.domain.Device;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceStatus;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class DeviceRepositoryIntegrationTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void saveDevice_whenDeviceIsValid_persistsDeviceAndCapabilities() {
        Device device = Device.builder()
                .deviceName("Kitchen Light")
                .deviceType(DeviceType.LIGHT)
                .status(DeviceStatus.ONLINE)
                .firmwareVersion("1.2.3")
                .registeredAt(LocalDateTime.now())
                .capabilities(List.of("DIMMABLE", "RGB_COLOR"))
                .connectivityType("WIFI")
                .build();

        Device saved = deviceRepository.saveAndFlush(device);

        assertThat(saved.getDeviceId()).isNotNull();
        assertThat(deviceRepository.findById(saved.getDeviceId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getDeviceName()).isEqualTo("Kitchen Light");
                    assertThat(found.getCapabilities()).containsExactlyInAnyOrder("DIMMABLE", "RGB_COLOR");
                });
        assertThat(deviceRepository.existsByDeviceName("Kitchen Light")).isTrue();
    }

    @Test
    void findById_whenDeviceDoesNotExist_returnsEmptyOptional() {
        assertThat(deviceRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void existsByDeviceName_whenDeviceNameDoesNotExist_returnsFalse() {
        assertThat(deviceRepository.existsByDeviceName("Missing Device")).isFalse();
    }
}
