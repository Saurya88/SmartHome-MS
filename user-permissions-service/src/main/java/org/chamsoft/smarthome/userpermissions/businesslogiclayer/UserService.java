package org.chamsoft.smarthome.userpermissions.businesslogiclayer;

import org.chamsoft.smarthome.userpermissions.presentationlayer.UserResponseDTO;
import org.chamsoft.smarthome.userpermissions.presentationlayer.UserRequestDTO;
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