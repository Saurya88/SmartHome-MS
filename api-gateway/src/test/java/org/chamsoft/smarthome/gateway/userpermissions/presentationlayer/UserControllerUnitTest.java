package org.chamsoft.smarthome.gateway.userpermissions.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chamsoft.smarthome.gateway.userpermissions.domainclientlayer.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
class UserControllerUnitTest {

    private WebTestClient webTestClient;

    @Autowired
    void init(WebApplicationContext webApplicationContext) {
        this.webTestClient = MockMvcWebTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_whenServiceReturnsData_returnsOk() throws Exception {
        UserResponseDTO user = UserResponseDTO.builder()
                .userId(UUID.randomUUID())
                .username("owner")
                .email("owner@smarthome.com")
                .role("OWNER")
                .accountStatus("ACTIVE")
                .permissions(List.of())
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(user));

        webTestClient.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.userResponseDTOList[0].username").isEqualTo("owner")
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void getUserById_whenServiceReturnsData_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponseDTO user = UserResponseDTO.builder()
                .userId(userId)
                .username("owner")
                .email("owner@smarthome.com")
                .role("OWNER")
                .accountStatus("ACTIVE")
                .permissions(List.of())
                .build();

        when(userService.getUserById(userId)).thenReturn(user);

        webTestClient.get()
                .uri("/api/v1/users/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId.toString())
                .jsonPath("$._links.self.href").exists();
    }

    @Test
    void createUser_whenServiceSucceeds_returnsCreated() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("owner")
                .email("owner@smarthome.com")
                .role("OWNER")
                .permissions(List.of())
                .build();

        UserResponseDTO user = UserResponseDTO.builder()
                .userId(userId)
                .username("owner")
                .email("owner@smarthome.com")
                .role("OWNER")
                .accountStatus("ACTIVE")
                .permissions(List.of())
                .build();

        when(userService.createUser(any())).thenReturn(user);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId.toString());
    }

    @Test
    void updateUser_whenServiceSucceeds_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("updated")
                .email("owner@smarthome.com")
                .role("OWNER")
                .permissions(List.of())
                .build();

        UserResponseDTO user = UserResponseDTO.builder()
                .userId(userId)
                .username("updated")
                .email("owner@smarthome.com")
                .role("OWNER")
                .accountStatus("ACTIVE")
                .permissions(List.of())
                .build();

        when(userService.updateUser(eq(userId), any())).thenReturn(user);

        webTestClient.put()
                .uri("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("updated");
    }

    @Test
    void deleteUser_whenServiceSucceeds_returnsNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).deleteUser(userId);

        webTestClient.delete()
                .uri("/api/v1/users/{id}", userId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void createUser_whenDownstreamReturnsConflict_returnsConflict() throws Exception {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .username("owner")
                .email("owner@smarthome.com")
                .role("OWNER")
                .permissions(List.of())
                .build();

        when(userService.createUser(any())).thenThrow(HttpClientErrorException.create(
                HttpStatus.CONFLICT,
                "Conflict",
                null,
                "{\"message\":\"Username already exists: owner\"}".getBytes(),
                null
        ));

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(requestDTO))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Username already exists: owner");
    }
}
