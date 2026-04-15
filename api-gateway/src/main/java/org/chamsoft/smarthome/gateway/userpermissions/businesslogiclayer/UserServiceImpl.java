package org.chamsoft.smarthome.gateway.userpermissions.businesslogiclayer;

import org.chamsoft.smarthome.gateway.userpermissions.presentationlayer.UserRequestDTO;
import org.chamsoft.smarthome.gateway.userpermissions.presentationlayer.UserResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final RestTemplate restTemplate;

    @Value("${microservices.user.base-url}")
    private String baseUrl;

    public UserServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        ResponseEntity<UserResponseDTO[]> response =
                restTemplate.getForEntity(baseUrl + "/api/v1/users", UserResponseDTO[].class);

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    @Override
    public UserResponseDTO getUserById(UUID userId) {
        return restTemplate.getForObject(
                baseUrl + "/api/v1/users/" + userId,
                UserResponseDTO.class
        );
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {
        return restTemplate.postForObject(
                baseUrl + "/api/v1/users",
                request,
                UserResponseDTO.class
        );
    }

    @Override
    public UserResponseDTO updateUser(UUID userId, UserRequestDTO request) {
        HttpEntity<UserRequestDTO> entity = new HttpEntity<>(request);

        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId,
                HttpMethod.PUT,
                entity,
                UserResponseDTO.class
        );

        return response.getBody();
    }

    @Override
    public void deleteUser(UUID userId) {
        restTemplate.delete(baseUrl + "/api/v1/users/" + userId);
    }
}