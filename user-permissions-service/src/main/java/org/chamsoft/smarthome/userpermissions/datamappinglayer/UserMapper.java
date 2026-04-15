package org.chamsoft.smarthome.userpermissions.datamappinglayer;

import org.chamsoft.smarthome.userpermissions.domain.*;
import org.chamsoft.smarthome.userpermissions.presentationlayer.*;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserResponseDTO toResponseDTO(User user) {
        if (user == null) return null;

        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                // Null-safe access to Value Object
                .email(user.getEmail() != null ? user.getEmail().getValue() : null)
                .role(user.getRole() != null ? user.getRole().name() : null)
                .accountStatus(user.getAccountStatus() != null ? user.getAccountStatus().name() : null)
                .permissions(safeMapPermissions(user.getPermissions()))
                .build();
    }

    private static List<UserResponseDTO.PermissionDTO> safeMapPermissions(List<Permission> permissions) {
        if (permissions == null) return List.of();
        return permissions.stream()
                .map(UserMapper::toPermissionDTO)
                .collect(Collectors.toList());
    }

    private static UserResponseDTO.PermissionDTO toPermissionDTO(Permission permission) {
        return UserResponseDTO.PermissionDTO.builder()
                .deviceId(permission.getDeviceId())
                .accessLevel(permission.getAccessLevel().name())
                .build();
    }

    public static User toEntity(UserRequestDTO dto) {
        if (dto == null) return null;

        List<Permission> permissions = dto.getPermissions() == null
                ? List.of()
                : dto.getPermissions().stream()
                .map(p -> Permission.builder()
                        .deviceId(p.getDeviceId())
                        // Use uppercase to match SQL/Enum naming conventions
                        .accessLevel(AccessLevel.valueOf(p.getAccessLevel().toUpperCase()))
                        .build())
                .collect(Collectors.toList());

        return User.builder()
                .username(dto.getUsername())
                .email(new Email(dto.getEmail()))
                // Normalizing input case
                .role(UserRole.valueOf(dto.getRole().toUpperCase()))
                .accountStatus(AccountStatus.ACTIVE)
                .permissions(permissions)
                .build();
    }
}