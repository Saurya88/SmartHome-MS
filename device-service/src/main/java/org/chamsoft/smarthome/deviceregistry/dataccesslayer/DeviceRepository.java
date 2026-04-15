package org.chamsoft.smarthome.deviceregistry.dataccesslayer;

import org.chamsoft.smarthome.deviceregistry.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    boolean existsByDeviceName(String deviceName);
}