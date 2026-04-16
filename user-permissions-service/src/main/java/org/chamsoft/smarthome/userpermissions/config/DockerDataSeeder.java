package org.chamsoft.smarthome.userpermissions.config;

import org.chamsoft.smarthome.userpermissions.dataccesslayer.UserRepository;
import org.chamsoft.smarthome.userpermissions.domain.AccessLevel;
import org.chamsoft.smarthome.userpermissions.domain.AccountStatus;
import org.chamsoft.smarthome.userpermissions.domain.Email;
import org.chamsoft.smarthome.userpermissions.domain.Permission;
import org.chamsoft.smarthome.userpermissions.domain.User;
import org.chamsoft.smarthome.userpermissions.domain.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Profile("docker")
public class DockerDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    public DockerDataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        seedUser(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "seed_owner",
                "owner.seed@smarthome.local",
                UserRole.OWNER,
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                AccessLevel.FULL_ACCESS
        );

        seedUser(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                "seed_member",
                "member.seed@smarthome.local",
                UserRole.MEMBER,
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                AccessLevel.READ_ONLY
        );
    }

    private void seedUser(UUID userId,
                          String username,
                          String email,
                          UserRole role,
                          UUID deviceId,
                          AccessLevel accessLevel) {
        if (userRepository.existsById(userId)) {
            return;
        }

        userRepository.save(User.builder()
                .userId(userId)
                .username(username)
                .email(new Email(email))
                .role(role)
                .accountStatus(AccountStatus.ACTIVE)
                .permissions(List.of(Permission.builder()
                        .deviceId(deviceId)
                        .accessLevel(accessLevel)
                        .build()))
                .build());
    }
}
