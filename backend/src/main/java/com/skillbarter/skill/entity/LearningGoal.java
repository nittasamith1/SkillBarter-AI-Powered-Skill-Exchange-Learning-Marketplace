package com.skillbarter.skill.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A user's learning goal targeting a specific skill.
 */
@Entity
@Table(name = "learning_goals")
public class LearningGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(updatable = false, nullable = false, length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", nullable = false, length = 36)
    private UUID userId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "tenant_id", nullable = false, length = 36)
    private UUID tenantId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "target_skill_id", nullable = false, length = 36)
    private UUID targetSkillId;

    @Column(name = "goal_text", columnDefinition = "TEXT")
    private String goalText;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level")
    private UserSkill.SkillLevel currentLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_level")
    private UserSkill.SkillLevel targetLevel;

    @Column
    private LocalDate deadline;

    @Column(name = "learning_preferences", columnDefinition = "TEXT")
    private String learningPreferences;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum GoalStatus {
        ACTIVE, COMPLETED, CANCELLED
    }

    public LearningGoal() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getTargetSkillId() { return targetSkillId; }
    public void setTargetSkillId(UUID targetSkillId) { this.targetSkillId = targetSkillId; }

    public String getGoalText() { return goalText; }
    public void setGoalText(String goalText) { this.goalText = goalText; }

    public UserSkill.SkillLevel getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(UserSkill.SkillLevel currentLevel) { this.currentLevel = currentLevel; }

    public UserSkill.SkillLevel getTargetLevel() { return targetLevel; }
    public void setTargetLevel(UserSkill.SkillLevel targetLevel) { this.targetLevel = targetLevel; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getLearningPreferences() { return learningPreferences; }
    public void setLearningPreferences(String learningPreferences) { this.learningPreferences = learningPreferences; }

    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
