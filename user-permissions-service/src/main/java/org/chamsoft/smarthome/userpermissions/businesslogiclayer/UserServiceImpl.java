package org.chamsoft.smarthome.userpermissions.businesslogiclayer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chamsoft.smarthome.userpermissions.dataccesslayer.UserRepository;
import org.chamsoft.smarthome.userpermissions.datamappinglayer.UserMapper;
import org.chamsoft.smarthome.userpermissions.domain.User;
import org.chamsoft.smarthome.userpermissions.presentationlayer.UserRequestDTO;
import org.chamsoft.smarthome.userpermissions.presentationlayer.UserResponseDTO;
import org.chamsoft.smarthome.userpermissions.utilities.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {

        User user = UserMapper.toEntity(dto);

        User savedUser = userRepository.save(user);

        return UserMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UUID userId, UserRequestDTO dto) {

        User existingUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User updatedUser = UserMapper.toEntity(dto);

        updatedUser.setUserId(existingUser.getUserId());

        User savedUser = userRepository.save(updatedUser);

        return UserMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {

        User existingUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.delete(existingUser);

        UserMapper.toResponseDTO(existingUser);
    }
}