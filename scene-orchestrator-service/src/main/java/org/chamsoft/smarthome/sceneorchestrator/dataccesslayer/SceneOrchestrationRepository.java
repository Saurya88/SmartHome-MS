package org.chamsoft.smarthome.sceneorchestrator.dataccesslayer;

import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SceneOrchestrationRepository extends JpaRepository<SceneOrchestration, UUID> {
}
