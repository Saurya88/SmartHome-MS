package org.chamsoft.smarthome.automation.dataccesslayer;

import org.chamsoft.smarthome.automation.domain.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SceneRepository extends JpaRepository<Scene, UUID> {
}
