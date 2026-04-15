package org.chamsoft.smarthome.gateway.userpermissions.businesslogiclayer;

import org.chamsoft.smarthome.gateway.userpermissions.presentationlayer.UserRequestDTO;
import org.chamsoft.smarthome.gateway.userpermissions.presentationlayer.UserResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface UserService {

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(UUID userId);

    UserResponseDTO createUser(UserRequestDTO dto);

    UserResponseDTO updateUser(UUID userId, UserRequestDTO dto);

    void deleteUser(UUID userId);
}