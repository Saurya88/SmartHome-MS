package org.chamsoft.smarthome.sceneorchestrator.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scene_orchestrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SceneOrchestration {

    @Id
    @Column(name = "orchestration_id", columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Builder.Default
    private UUID orchestrationId = UUID.randomUUID();

    @Column(name = "owner_user_id", columnDefinition = "VARCHAR(36)", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID ownerUserId;

    @Column(name = "automation_scene_id", columnDefinition = "VARCHAR(36)", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID automationSceneId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SceneOrchestrationStatus status = SceneOrchestrationStatus.INACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection
    @CollectionTable(name = "scene_orchestration_device_actions", joinColumns = @JoinColumn(name = "orchestration_id"))
    @Builder.Default
    private List<ManagedDeviceAction> deviceActions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "scene_orchestration_trigger_rules", joinColumns = @JoinColumn(name = "orchestration_id"))
    @Builder.Default
    private List<ManagedTriggerRule> triggerRules = new ArrayList<>();
}
