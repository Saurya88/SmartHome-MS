package org.chamsoft.smarthome.deviceregistry.businesslogiclayer;

import org.chamsoft.smarthome.deviceregistry.domain.Device;
import org.chamsoft.smarthome.deviceregistry.dataccesslayer.DeviceRepository;
import org.chamsoft.smarthome.deviceregistry.utilities.DuplicateDeviceNameException;
import org.chamsoft.smarthome.deviceregistry.utilities.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(UUID deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));
    }

    public Device createDevice(Device device) {
        if (deviceRepository.existsByDeviceName(device.getDeviceName())) {
            throw new DuplicateDeviceNameException(device.getDeviceName());
        }
        return deviceRepository.save(device);
    }

    public Device updateDevice(UUID deviceId, Device updatedDevice) {
        Device existing = getDeviceById(deviceId);

        existing.setDeviceName(updatedDevice.getDeviceName());
        existing.setDeviceType(updatedDevice.getDeviceType());
        existing.setStatus(updatedDevice.getStatus());
        existing.setFirmwareVersion(updatedDevice.getFirmwareVersion());
        existing.setCapabilities(updatedDevice.getCapabilities());
        existing.setConnectivityType(updatedDevice.getConnectivityType());

        return deviceRepository.save(existing);
    }

    public void deleteDevice(UUID deviceId) {
        deviceRepository.deleteById(deviceId);
    }
}
