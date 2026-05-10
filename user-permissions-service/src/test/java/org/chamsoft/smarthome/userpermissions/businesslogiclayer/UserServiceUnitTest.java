package org.chamsoft.smarthome.userpermissions.businesslogiclayer;

import org.chamsoft.smarthome.userpermissions.dataccesslayer.UserRepository;
import org.chamsoft.smarthome.userpermissions.domain.AccessLevel;
import org.chamsoft.smarthome.userpermissions.domain.AccountStatus;
import org.chamsoft.smarthome.userpermissions.domain.Email;
import org.chamsoft.smarthome.userpermissions.domain.Permission;
import org.chamsoft.smarthome.userpermissions.domain.User;
import org.chamsoft.smarthome.userpermissions.domain.UserRole;
import org.chamsoft.smarthome.userpermissions.presentationlayer.UserRequestDTO;
import org.chamsoft.smarthome.userpermissions.presentationlayer.UserResponseDTO;
import org.chamsoft.smarthome.userpermissions.utilities.DuplicateUsernameException;
import org.chamsoft.smarthome.userpermissions.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_whenUsernameIsUnique_savesUser() {
        UserRequestDTO requestDTO = userRequest("owner", "owner@smarthome.com");
        when(userRepository.findByUsername("owner")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO responseDTO = userService.createUser(requestDTO);

        assertThat(responseDTO.getUsername()).isEqualTo("owner");
        assertThat(responseDTO.getAccountStatus()).isEqualTo("ACTIVE");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_whenUsernameExists_throwsDuplicateUsernameException() {
        UserRequestDTO requestDTO = userRequest("owner", "owner@smarthome.com");
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(user(UUID.randomUUID(), "owner")));

        assertThatThrownBy(() -> userService.createUser(requestDTO))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessage("Username already exists: owner");
    }

    @Test
    void getUserById_whenMissing_throwsResourceNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void updateUser_whenUserExists_keepsExistingUserId() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user(userId, "old_owner")));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO responseDTO = userService.updateUser(userId, userRequest("new_owner", "new@smarthome.com"));

        assertThat(responseDTO.getUserId()).isEqualTo(userId);
        assertThat(responseDTO.getUsername()).isEqualTo("new_owner");
        assertThat(responseDTO.getEmail()).isEqualTo("new@smarthome.com");
    }

    private UserRequestDTO userRequest(String username, String email) {
        return UserRequestDTO.builder()
                .username(username)
                .email(email)
                .role("OWNER")
                .permissions(List.of(UserRequestDTO.PermissionDTO.builder()
                        .deviceId(UUID.randomUUID())
                        .accessLevel("FULL_ACCESS")
                        .build()))
                .build();
    }

    private User user(UUID userId, String username) {
        return User.builder()
                .userId(userId)
                .username(username)
                .email(new Email(username + "@smarthome.com"))
                .role(UserRole.OWNER)
                .accountStatus(AccountStatus.ACTIVE)
                .permissions(List.of(Permission.builder()
                        .deviceId(UUID.randomUUID())
                        .accessLevel(AccessLevel.FULL_ACCESS)
                        .build()))
                .build();
    }
}
