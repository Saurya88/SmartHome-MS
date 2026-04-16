package org.chamsoft.smarthome.gateway.userpermissions.presentationlayer;

import org.chamsoft.smarthome.gateway.userpermissions.domainclientlayer.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDTO>>> getAllUsers() {
        List<EntityModel<UserResponseDTO>> users = userService.getAllUsers().stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(
                users,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel()
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<EntityModel<UserResponseDTO>> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(toModel(userService.getUserById(userId)));
    }

    @PostMapping
    public ResponseEntity<EntityModel<UserResponseDTO>> createUser(@RequestBody UserRequestDTO requestDTO) {
        UserResponseDTO created = userService.createUser(requestDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(created.getUserId())
                .toUri();

        return ResponseEntity.created(location).body(toModel(created));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<EntityModel<UserResponseDTO>> updateUser(@PathVariable UUID userId,
                                                                   @RequestBody UserRequestDTO requestDTO) {
        return ResponseEntity.ok(toModel(userService.updateUser(userId, requestDTO)));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<UserResponseDTO> toModel(UserResponseDTO user) {
        return EntityModel.of(
                user,
                linkTo(methodOn(UserController.class).getUserById(user.getUserId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")
        );
    }
}
