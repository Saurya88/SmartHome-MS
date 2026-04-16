package org.chamsoft.smarthome.userpermissions.datamappinglayer;

import org.chamsoft.smarthome.userpermissions.domain.AccountStatus;
import org.chamsoft.smarthome.userpermissions.domain.Email;
import org.chamsoft.smarthome.userpermissions.domain.User;
import org.chamsoft.smarthome.userpermissions.domain.UserRole;
import org.chamsoft.smarthome.userpermissions.presentationlayer.UserRequestDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toEntity_whenDtoIsNull_returnsNull() {
        assertThat(UserMapper.toEntity(null)).isNull();
    }

    @Test
    void toResponseDTO_whenUserIsNull_returnsNull() {
        assertThat(UserMapper.toResponseDTO(null)).isNull();
    }

    @Test
    void toResponseDTO_whenOptionalFieldsAreNull_returnsNullFieldsAndEmptyPermissions() {
        User user = User.builder()
                .username("minimal")
                .email(null)
                .role(null)
                .accountStatus(null)
                .permissions(null)
                .build();

        var dto = UserMapper.toResponseDTO(user);

        assertThat(dto.getUsername()).isEqualTo("minimal");
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getRole()).isNull();
        assertThat(dto.getAccountStatus()).isNull();
        assertThat(dto.getPermissions()).isEmpty();
    }

    @Test
    void toEntity_whenPermissionsAreNull_usesEmptyPermissionsAndActiveStatus() {
        UserRequestDTO request = UserRequestDTO.builder()
                .username("admin_user")
                .email("admin@example.com")
                .role("admin")
                .permissions(null)
                .build();

        User user = UserMapper.toEntity(request);

        assertThat(user.getUsername()).isEqualTo("admin_user");
        assertThat(user.getEmail().getValue()).isEqualTo("admin@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.getPermissions()).isEmpty();
    }

    @Test
    void toResponseDTO_whenUserIsValid_mapsFields() {
        User user = User.builder()
                .username("owner")
                .email(new Email("owner@example.com"))
                .role(UserRole.OWNER)
                .accountStatus(AccountStatus.ACTIVE)
                .permissions(List.of())
                .build();

        var dto = UserMapper.toResponseDTO(user);

        assertThat(dto.getEmail()).isEqualTo("owner@example.com");
        assertThat(dto.getRole()).isEqualTo("OWNER");
        assertThat(dto.getAccountStatus()).isEqualTo("ACTIVE");
    }
}
