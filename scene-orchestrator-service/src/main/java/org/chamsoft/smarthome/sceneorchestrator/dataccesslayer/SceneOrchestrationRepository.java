package org.chamsoft.smarthome.sceneorchestrator.dataccesslayer;

import org.chamsoft.smarthome.sceneorchestrator.domain.SceneOrchestration;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface SceneOrchestrationRepository extends MongoRepository<SceneOrchestration, UUID> {
}

