package com.skillbarter.skill.repository;

import com.skillbarter.skill.entity.LearningGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningGoalRepository extends JpaRepository<LearningGoal, UUID> {

    List<LearningGoal> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    List<LearningGoal> findByUserIdAndTenantIdAndStatus(
            UUID userId, UUID tenantId, LearningGoal.GoalStatus status);

    Optional<LearningGoal> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndTargetSkillId(UUID userId, UUID targetSkillId);
}
