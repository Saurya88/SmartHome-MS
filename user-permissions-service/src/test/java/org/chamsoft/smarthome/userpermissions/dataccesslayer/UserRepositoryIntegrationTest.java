package org.chamsoft.smarthome.userpermissions.dataccesslayer;

import org.chamsoft.smarthome.userpermissions.domain.AccessLevel;
import org.chamsoft.smarthome.userpermissions.domain.AccountStatus;
import org.chamsoft.smarthome.userpermissions.domain.Email;
import org.chamsoft.smarthome.userpermissions.domain.Permission;
import org.chamsoft.smarthome.userpermissions.domain.User;
import org.chamsoft.smarthome.userpermissions.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveUser_whenUserIsValid_persistsUserAndPermissions() {
        UUID deviceId = UUID.randomUUID();
        User user = User.builder()
                .username("owner_user")
                .email(new Email("owner@example.com"))
                .role(UserRole.OWNER)
                .accountStatus(AccountStatus.ACTIVE)
                .permissions(List.of(new Permission(deviceId, AccessLevel.FULL_ACCESS)))
                .build();

        User saved = userRepository.saveAndFlush(user);

        assertThat(saved.getUserId()).isNotNull();
        assertThat(userRepository.findByUserId(saved.getUserId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getUsername()).isEqualTo("owner_user");
                    assertThat(found.getEmail().getValue()).isEqualTo("owner@example.com");
                    assertThat(found.getPermissions()).hasSize(1);
                    assertThat(found.getPermissions().get(0).getDeviceId()).isEqualTo(deviceId);
                });
        assertThat(userRepository.findByUsername("owner_user")).isPresent();
        assertThat(userRepository.findByEmail_Value("owner@example.com")).isPresent();
    }

    @Test
    void findByUserId_whenUserDoesNotExist_returnsEmptyOptional() {
        assertThat(userRepository.findByUserId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByEmailValue_whenEmailDoesNotExist_returnsEmptyOptional() {
        assertThat(userRepository.findByEmail_Value("missing@example.com")).isEmpty();
    }
}
