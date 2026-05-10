package org.chamsoft.smarthome.userpermissions.presentationlayer;

import org.chamsoft.smarthome.userpermissions.businesslogiclayer.UserService;
import org.chamsoft.smarthome.userpermissions.utilities.DuplicateUsernameException;
import org.chamsoft.smarthome.userpermissions.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
class UserControllerUnitTest {

    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Test
    void getUserById_whenServiceReturnsUser_returnsOk() {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId)).thenReturn(userResponse(userId, "owner"));

        webTestClient.get()
                .uri("/api/v1/users/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId.toString())
                .jsonPath("$.username").isEqualTo("owner");
    }

    @Test
    void createUser_whenServiceReturnsUser_returnsCreated() {
        UUID userId = UUID.randomUUID();
        when(userService.createUser(any())).thenReturn(userResponse(userId, "owner"));

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest("owner"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("Location", ".*/api/v1/users/" + userId)
                .expectBody()
                .jsonPath("$.username").isEqualTo("owner");
    }

    @Test
    void getUserById_whenServiceThrowsNotFound_returnsNotFound() {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId)).thenThrow(new ResourceNotFoundException("User not found"));

        webTestClient.get()
                .uri("/api/v1/users/{id}", userId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("User not found");
    }

    @Test
    void createUser_whenUsernameExists_returnsConflict() {
        when(userService.createUser(any())).thenThrow(new DuplicateUsernameException("owner"));

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest("owner"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Username already exists: owner");
    }

    @Test
    void deleteUser_whenServiceThrowsNotFound_returnsNotFound() {
        UUID userId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(userId);

        webTestClient.delete()
                .uri("/api/v1/users/{id}", userId)
                .exchange()
                .expectStatus().isNotFound();
    }

    private UserResponseDTO userResponse(UUID userId, String username) {
        return UserResponseDTO.builder()
                .userId(userId)
                .username(username)
                .email(username + "@smarthome.com")
                .role("OWNER")
                .accountStatus("ACTIVE")
                .permissions(List.of())
                .build();
    }

    private Map<String, Object> userRequest(String username) {
        return Map.of(
                "username", username,
                "email", username + "@smarthome.com",
                "role", "OWNER",
                "permissions", List.of(Map.of(
                        "deviceId", UUID.randomUUID(),
                        "accessLevel", "FULL_ACCESS"
                ))
        );
    }
}
