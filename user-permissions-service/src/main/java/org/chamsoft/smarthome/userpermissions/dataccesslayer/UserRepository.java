package org.chamsoft.smarthome.userpermissions.dataccesslayer;

import org.chamsoft.smarthome.userpermissions.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserId(UUID userId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail_Value(String email);

}