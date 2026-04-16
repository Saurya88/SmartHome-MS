package org.chamsoft.smarthome.deviceregistry.config;

import org.chamsoft.smarthome.deviceregistry.dataccesslayer.DeviceRepository;
import org.chamsoft.smarthome.deviceregistry.domain.Device;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceStatus;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Profile("docker")
public class DockerDataSeeder implements CommandLineRunner {

    private final DeviceRepository deviceRepository;

    public DockerDataSeeder(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void run(String... args) {
        seedDevice(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Seed Living Room Light",
                DeviceType.LIGHT,
                "1.0.0",
                List.of("TURN_ON", "TURN_OFF"),
                "WIFI"
        );

        seedDevice(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Seed Hallway Sensor",
                DeviceType.SENSOR,
                "2.1.0",
                List.of("READ_TEMPERATURE"),
                "ETHERNET"
        );
    }

    private void seedDevice(UUID deviceId,
                            String name,
                            DeviceType type,
                            String firmwareVersion,
                            List<String> capabilities,
                            String connectivityType) {
        if (deviceRepository.existsById(deviceId)) {
            return;
        }

        deviceRepository.save(Device.builder()
                .deviceId(deviceId)
                .deviceName(name)
                .deviceType(type)
                .status(DeviceStatus.ONLINE)
                .firmwareVersion(firmwareVersion)
                .registeredAt(LocalDateTime.of(2026, 1, 1, 9, 0))
                .capabilities(capabilities)
                .connectivityType(connectivityType)
                .build());
    }
}
