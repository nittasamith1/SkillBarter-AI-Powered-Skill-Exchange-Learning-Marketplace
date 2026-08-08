package com.skillbarter.skill.service;

import com.skillbarter.common.exception.BusinessException;
import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.common.security.TenantContext;
import com.skillbarter.skill.dto.AddUserSkillRequest;
import com.skillbarter.skill.dto.UpdateUserSkillRequest;
import com.skillbarter.skill.dto.UserSkillResponse;
import com.skillbarter.skill.entity.Skill;
import com.skillbarter.skill.entity.SkillCategory;
import com.skillbarter.skill.entity.UserSkill;
import com.skillbarter.skill.repository.SkillCategoryRepository;
import com.skillbarter.skill.repository.SkillRepository;
import com.skillbarter.skill.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final SkillCategoryRepository categoryRepository;

    public UserSkillService(
            UserSkillRepository userSkillRepository,
            SkillRepository skillRepository,
            SkillCategoryRepository categoryRepository) {
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<UserSkillResponse> getUserSkills(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResourceNotFoundException("No tenant context available");
        }

        List<UserSkill> userSkills = userSkillRepository.findByUserIdAndTenantId(userId, tenantId);
        return mapToResponses(userSkills);
    }

    @Transactional
    public UserSkillResponse addUserSkill(UUID userId, AddUserSkillRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResourceNotFoundException("No tenant context available");
        }

        Skill skill = skillRepository.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + request.skillId()));

        if (userSkillRepository.existsByUserIdAndSkillId(userId, request.skillId())) {
            throw new BusinessException("Skill is already added to user profile");
        }

        UserSkill userSkill = new UserSkill();
        userSkill.setUserId(userId);
        userSkill.setTenantId(tenantId);
        userSkill.setSkillId(skill.getId());
        userSkill.setLevel(request.level());
        userSkill.setCanTeach(request.canTeach());
        userSkill.setWantToLearn(request.wantToLearn());
        userSkill.setYearsExperience(request.yearsExperience());

        UserSkill saved = userSkillRepository.save(userSkill);
        String categoryName = getCategoryName(skill.getCategoryId());

        return UserSkillResponse.from(saved, skill.getName(), categoryName);
    }

    @Transactional
    public UserSkillResponse updateUserSkill(UUID userId, UUID skillId, UpdateUserSkillRequest request) {
        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill on user profile not found"));

        if (request.level() != null) userSkill.setLevel(request.level());
        if (request.canTeach() != null) userSkill.setCanTeach(request.canTeach());
        if (request.wantToLearn() != null) userSkill.setWantToLearn(request.wantToLearn());
        if (request.yearsExperience() != null) userSkill.setYearsExperience(request.yearsExperience());

        UserSkill updated = userSkillRepository.save(userSkill);
        Skill skill = skillRepository.findById(skillId).orElse(null);
        String skillName = skill != null ? skill.getName() : "Unknown";
        String categoryName = skill != null ? getCategoryName(skill.getCategoryId()) : "General";

        return UserSkillResponse.from(updated, skillName, categoryName);
    }

    @Transactional
    public void removeUserSkill(UUID userId, UUID skillId) {
        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill on user profile not found"));
        userSkillRepository.delete(userSkill);
    }

    private List<UserSkillResponse> mapToResponses(List<UserSkill> userSkills) {
        if (userSkills.isEmpty()) return List.of();

        List<UUID> skillIds = userSkills.stream().map(UserSkill::getSkillId).toList();
        Map<UUID, Skill> skillMap = skillRepository.findAllById(skillIds).stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));

        Map<UUID, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(SkillCategory::getId, SkillCategory::getName));

        return userSkills.stream().map(us -> {
            Skill s = skillMap.get(us.getSkillId());
            String skillName = s != null ? s.getName() : "Unknown";
            String categoryName = s != null ? categoryNames.getOrDefault(s.getCategoryId(), "General") : "General";
            return UserSkillResponse.from(us, skillName, categoryName);
        }).toList();
    }

    private String getCategoryName(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .map(SkillCategory::getName)
                .orElse("General");
    }
}
