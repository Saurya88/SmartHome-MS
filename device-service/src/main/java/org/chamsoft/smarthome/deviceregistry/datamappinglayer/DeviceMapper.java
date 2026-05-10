package org.chamsoft.smarthome.deviceregistry.datamappinglayer;

import org.chamsoft.smarthome.deviceregistry.domain.Device;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceStatus;
import org.chamsoft.smarthome.deviceregistry.domain.DeviceType;
import org.chamsoft.smarthome.deviceregistry.presentationlayer.DeviceRequestDTO;
import org.chamsoft.smarthome.deviceregistry.presentationlayer.DeviceResponseDTO;

import java.time.LocalDateTime;

public class DeviceMapper {

    public static Device toEntity(DeviceRequestDTO dto) {
        if (dto == null) return null;

        DeviceStatus status = dto.getStatus() == null || dto.getStatus().isBlank()
                ? DeviceStatus.OFFLINE
                : DeviceStatus.valueOf(dto.getStatus());

        return Device.builder()
                .deviceName(dto.getDeviceName())
                .deviceType(DeviceType.valueOf(dto.getDeviceType()))
                .status(status)
                .firmwareVersion(dto.getFirmwareVersion())
                .registeredAt(LocalDateTime.now())
                .capabilities(dto.getCapabilities()) // now just List<String>
                .connectivityType(dto.getConnectivityType())
                .build();
    }

    public static DeviceResponseDTO toDTO(Device device) {
        if (device == null) return null;

        return DeviceResponseDTO.builder()
                .deviceId(device.getDeviceId())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType().name())
                .status(device.getStatus().name())
                .firmwareVersion(device.getFirmwareVersion())
                .registeredAt(device.getRegisteredAt())
                .capabilities(device.getCapabilities()) // List<String> directly
                .connectivityType(device.getConnectivityType())
                .build();
    }
}
