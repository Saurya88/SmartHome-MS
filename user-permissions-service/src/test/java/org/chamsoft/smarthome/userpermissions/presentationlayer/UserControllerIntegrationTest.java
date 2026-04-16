package org.chamsoft.smarthome.userpermissions.presentationlayer;

import org.chamsoft.smarthome.userpermissions.dataccesslayer.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.sql.init.mode=never"
)
@AutoConfigureWebTestClient
class UserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createAndGetUser_whenRequestIsValid_returnsCreatedUser() {
        UserResponseDTO created = createUser("home_owner", "owner@example.com", "OWNER");

        webTestClient.get()
                .uri("/api/v1/users/{id}", created.getUserId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .value(response -> {
                    assertThat(response.getUsername()).isEqualTo("home_owner");
                    assertThat(response.getEmail()).isEqualTo("owner@example.com");
                    assertThat(response.getAccountStatus()).isEqualTo("ACTIVE");
                    assertThat(response.getPermissions()).hasSize(1);
                });
    }

    @Test
    void getAllUsers_whenUsersExist_returnsCollection() {
        createUser("first_user", "first@example.com", "MEMBER");
        createUser("second_user", "second@example.com", "GUEST");

        webTestClient.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponseDTO.class)
                .value(response -> assertThat(response).hasSize(2));
    }

    @Test
    void updateUser_whenUserExists_returnsUpdatedUser() {
        UserResponseDTO created = createUser("member_user", "member@example.com", "MEMBER");

        webTestClient.put()
                .uri("/api/v1/users/{id}", created.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest("updated_member", "updated@example.com", "ADMIN"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDTO.class)
                .value(response -> {
                    assertThat(response.getUserId()).isEqualTo(created.getUserId());
                    assertThat(response.getUsername()).isEqualTo("updated_member");
                    assertThat(response.getRole()).isEqualTo("ADMIN");
                });
    }

    @Test
    void deleteUser_whenUserExists_returnsNoContent() {
        UserResponseDTO created = createUser("delete_user", "delete@example.com", "GUEST");

        webTestClient.delete()
                .uri("/api/v1/users/{id}", created.getUserId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(userRepository.findByUserId(created.getUserId())).isEmpty();
    }

    @Test
    void getUserById_whenUserDoesNotExist_returnsNotFound() {
        webTestClient.get()
                .uri("/api/v1/users/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("User not found");
    }

    @Test
    void createUser_whenUsernameAlreadyExists_returnsConflict() {
        createUser("duplicate_user", "first@example.com", "MEMBER");

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest("duplicate_user", "second@example.com", "MEMBER"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.message").isEqualTo("Username already exists: duplicate_user");
    }

    private UserResponseDTO createUser(String username, String email, String role) {
        return webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest(username, email, role))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDTO.class)
                .returnResult()
                .getResponseBody();
    }

    private Map<String, Object> userRequest(String username, String email, String role) {
        return Map.of(
                "username", username,
                "email", email,
                "role", role,
                "permissions", List.of(Map.of(
                        "deviceId", UUID.randomUUID(),
                        "accessLevel", "FULL_ACCESS"
                ))
        );
    }
}
