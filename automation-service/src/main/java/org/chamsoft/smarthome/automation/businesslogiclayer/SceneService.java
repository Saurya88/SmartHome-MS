package org.chamsoft.smarthome.automation.businesslogiclayer;

import org.chamsoft.smarthome.automation.dataccesslayer.SceneRepository;
import org.chamsoft.smarthome.automation.domain.*;
import org.chamsoft.smarthome.automation.presentationlayer.SceneRequestDTO;
import org.chamsoft.smarthome.automation.utilities.InvalidSceneActivationException;
import org.chamsoft.smarthome.automation.utilities.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SceneService {

    private final SceneRepository sceneRepository;

    public SceneService(SceneRepository sceneRepository) {
        this.sceneRepository = sceneRepository;
    }

    @Transactional
    public Scene createScene(String name) {
        Scene scene = new Scene(name);
        return sceneRepository.save(scene);
    }

    @Transactional
    public Scene updateScene(UUID sceneId, SceneRequestDTO request) {
        Scene scene = getSceneOrThrow(sceneId);

        if (request.getName() != null) {
            scene.setName(request.getName());
        }

        if (request.getStatus() != null) {
            scene.setStatus(request.getStatus());
        }

        return sceneRepository.save(scene);
    }

    @Transactional
    public Scene addDeviceActions(UUID sceneId, List<DeviceAction> actions) {
        Scene scene = getSceneOrThrow(sceneId);
        actions.forEach(scene::addDeviceAction);
        return sceneRepository.save(scene);
    }

    @Transactional
    public Scene addTriggerRules(UUID sceneId, List<TriggerRule> rules) {
        Scene scene = getSceneOrThrow(sceneId);
        rules.forEach(scene::addTriggerRule);
        return sceneRepository.save(scene);
    }

    @Transactional
    public Scene activateScene(UUID sceneId) {
        Scene scene = getSceneOrThrow(sceneId);
        try {
            scene.activate();
        } catch (IllegalStateException ex) {
            throw new InvalidSceneActivationException(ex.getMessage());
        }
        return sceneRepository.save(scene);
    }

    public Scene getSceneOrThrow(UUID sceneId) {
        return sceneRepository.findById(sceneId)
                .orElseThrow(() -> new ResourceNotFoundException("Scene not found"));
    }

    public List<Scene> getAllScenes() {
        return sceneRepository.findAll();
    }

    @Transactional
    public void deleteScene(UUID sceneId) {
        sceneRepository.deleteById(sceneId);
    }
}
