package org.chamsoft.smarthome.automation.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@Table(name = "scenes")
public class Scene {

    @Id
    @Column(name = "scene_id", columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID sceneId;

    private String name;

    @Enumerated(EnumType.STRING)
    private SceneStatus status;

    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "scene_device_actions", joinColumns = @JoinColumn(name = "scene_id"))
    private List<DeviceAction> deviceActions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "scene_trigger_rules",
            joinColumns = @JoinColumn(name = "scene_id"),
            inverseJoinColumns = @JoinColumn(name = "trigger_id")
    )
    private List<TriggerRule> triggerRules = new ArrayList<>();
    public Scene() {
        this.sceneId = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.status = SceneStatus.INACTIVE;
    }

    public Scene(String name) {
        this();
        this.name = name;
    }

    public void addDeviceAction(DeviceAction action) {
        deviceActions.add(action);
    }

    public void addTriggerRule(TriggerRule rule) {
        triggerRules.add(rule);
    }

    public void activate() {
        if (deviceActions.isEmpty() || triggerRules.isEmpty()) {
            throw new IllegalStateException("Scene must contain at least one TriggerRule and one DeviceAction to activate.");
        }
        this.status = SceneStatus.ACTIVE;
    }

}
