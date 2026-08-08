package com.skillbarter.skill.service;

import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.skill.dto.CreateLearningGoalRequest;
import com.skillbarter.skill.dto.LearningGoalResponse;
import com.skillbarter.skill.dto.UpdateLearningGoalRequest;
import com.skillbarter.skill.entity.LearningGoal;
import com.skillbarter.skill.entity.Skill;
import com.skillbarter.skill.repository.LearningGoalRepository;
import com.skillbarter.skill.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LearningGoalService {

    private final LearningGoalRepository goalRepository;
    private final SkillRepository skillRepository;

    public LearningGoalService(LearningGoalRepository goalRepository, SkillRepository skillRepository) {
        this.goalRepository = goalRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public List<LearningGoalResponse> getUserGoals(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResourceNotFoundException("No tenant context available");
        }

        List<LearningGoal> goals = goalRepository.findByUserIdAndTenantId(userId, tenantId);
        return mapToResponses(goals);
    }

    @Transactional
    public LearningGoalResponse createGoal(UUID userId, CreateLearningGoalRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResourceNotFoundException("No tenant context available");
        }

        Skill skill = skillRepository.findById(request.targetSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Target skill not found with id: " + request.targetSkillId()));

        if (goalRepository.existsByUserIdAndTargetSkillId(userId, request.targetSkillId())) {
            throw new BusinessException("A learning goal for this skill already exists");
        }

        LearningGoal goal = new LearningGoal();
        goal.setUserId(userId);
        goal.setTenantId(tenantId);
        goal.setTargetSkillId(skill.getId());
        goal.setGoalText(request.goalText());
        goal.setCurrentLevel(request.currentLevel());
        goal.setTargetLevel(request.targetLevel());
        goal.setDeadline(request.deadline());
        goal.setLearningPreferences(request.learningPreferences());
        goal.setStatus(LearningGoal.GoalStatus.ACTIVE);

        LearningGoal saved = goalRepository.save(goal);
        return LearningGoalResponse.from(saved, skill.getName());
    }

    @Transactional
    public LearningGoalResponse updateGoal(UUID userId, UUID goalId, UpdateLearningGoalRequest request) {
        LearningGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning goal not found"));

        if (request.goalText() != null) goal.setGoalText(request.goalText());
        if (request.currentLevel() != null) goal.setCurrentLevel(request.currentLevel());
        if (request.targetLevel() != null) goal.setTargetLevel(request.targetLevel());
        if (request.deadline() != null) goal.setDeadline(request.deadline());
        if (request.learningPreferences() != null) goal.setLearningPreferences(request.learningPreferences());
        if (request.status() != null) goal.setStatus(request.status());

        LearningGoal updated = goalRepository.save(goal);
        String skillName = skillRepository.findById(updated.getTargetSkillId())
                .map(Skill::getName).orElse("Unknown");

        return LearningGoalResponse.from(updated, skillName);
    }

    @Transactional
    public void deleteGoal(UUID userId, UUID goalId) {
        LearningGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning goal not found"));
        goalRepository.delete(goal);
    }

    private List<LearningGoalResponse> mapToResponses(List<LearningGoal> goals) {
        if (goals.isEmpty()) return List.of();

        List<UUID> skillIds = goals.stream().map(LearningGoal::getTargetSkillId).toList();
        Map<UUID, String> skillNames = skillRepository.findAllById(skillIds).stream()
                .collect(Collectors.toMap(Skill::getId, Skill::getName));

        return goals.stream()
                .map(g -> LearningGoalResponse.from(g, skillNames.getOrDefault(g.getTargetSkillId(), "Unknown")))
                .toList();
    }
}
