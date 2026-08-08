package com.skillbarter.skill.service;

import com.skillbarter.common.exception.ResourceNotFoundException;
import com.skillbarter.skill.dto.CreateSkillRequest;
import com.skillbarter.skill.dto.ExploreSkillResponse;
import com.skillbarter.skill.dto.SkillCategoryResponse;
import com.skillbarter.skill.dto.SkillResponse;
import com.skillbarter.skill.entity.Skill;
import com.skillbarter.skill.entity.SkillCategory;
import com.skillbarter.skill.entity.SkillPrerequisite;
import com.skillbarter.skill.repository.SkillCategoryRepository;
import com.skillbarter.skill.repository.SkillPrerequisiteRepository;
import com.skillbarter.skill.repository.SkillRepository;
import com.skillbarter.skill.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SkillService {

    private final SkillCategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final SkillPrerequisiteRepository prerequisiteRepository;
    private final UserSkillRepository userSkillRepository;

    public SkillService(
            SkillCategoryRepository categoryRepository,
            SkillRepository skillRepository,
            SkillPrerequisiteRepository prerequisiteRepository,
            UserSkillRepository userSkillRepository) {
        this.categoryRepository = categoryRepository;
        this.skillRepository = skillRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.userSkillRepository = userSkillRepository;
    }

    @Transactional(readOnly = true)
    public List<SkillCategoryResponse> getCategoryTree() {
        List<SkillCategory> allCategories = categoryRepository.findAll();
        Map<UUID, List<SkillCategory>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(SkillCategory::getParentId));

        return allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .map(root -> buildCategoryNode(root, childrenMap))
                .toList();
    }

    private SkillCategoryResponse buildCategoryNode(SkillCategory category, Map<UUID, List<SkillCategory>> childrenMap) {
        List<SkillCategory> children = childrenMap.getOrDefault(category.getId(), List.of());
        List<SkillCategoryResponse> childResponses = children.stream()
                .map(child -> buildCategoryNode(child, childrenMap))
                .toList();
        return SkillCategoryResponse.from(category, childResponses);
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> searchSkills(String search, UUID categoryId) {
        List<Skill> skills;
        if (search != null && !search.isBlank() && categoryId != null) {
            skills = skillRepository.searchByNameAndCategory(search.trim(), categoryId);
        } else if (search != null && !search.isBlank()) {
            skills = skillRepository.searchByName(search.trim());
        } else if (categoryId != null) {
            skills = skillRepository.findByCategoryIdAndGlobal(categoryId);
        } else {
            skills = skillRepository.findByIsGlobalTrue();
        }

        Map<UUID, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(SkillCategory::getId, SkillCategory::getName));

        return skills.stream()
                .map(s -> mapToResponse(s, categoryNames.getOrDefault(s.getCategoryId(), "General")))
                .toList();
    }

    @Transactional(readOnly = true)
    public SkillResponse getSkillById(UUID id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));

        String categoryName = categoryRepository.findById(skill.getCategoryId())
                .map(SkillCategory::getName)
                .orElse("General");

        return mapToResponse(skill, categoryName);
    }

    @Transactional(readOnly = true)
    public List<ExploreSkillResponse> exploreSkills(String search, UUID categoryId) {
        List<SkillResponse> skills = searchSkills(search, categoryId);

        return skills.stream().map(s -> {
            long teacherCount = userSkillRepository.countTeachersBySkillId(s.id());
            long learnerCount = userSkillRepository.countLearnersBySkillId(s.id());
            return new ExploreSkillResponse(
                    s.id(), s.name(), s.description(), s.categoryId(), s.categoryName(), teacherCount, learnerCount
            );
        }).toList();
    }

    @Transactional
    public SkillResponse createSkill(CreateSkillRequest request) {
        if (!categoryRepository.existsById(request.categoryId())) {
            throw new ResourceNotFoundException("Category not found with id: " + request.categoryId());
        }

        Skill skill = new Skill();
        skill.setName(request.name());
        skill.setDescription(request.description());
        skill.setCategoryId(request.categoryId());
        skill.setGlobal(true);

        if (request.tags() != null && !request.tags().isEmpty()) {
            skill.setTags("[" + request.tags().stream().map(t -> "\"" + t + "\"").collect(Collectors.joining(",")) + "]");
        }

        Skill saved = skillRepository.save(skill);

        if (request.prerequisiteSkillIds() != null) {
            for (UUID prereqId : request.prerequisiteSkillIds()) {
                if (skillRepository.existsById(prereqId)) {
                    prerequisiteRepository.save(new SkillPrerequisite(saved.getId(), prereqId));
                }
            }
        }

        String categoryName = categoryRepository.findById(saved.getCategoryId())
                .map(SkillCategory::getName).orElse("General");

        return mapToResponse(saved, categoryName);
    }

    private SkillResponse mapToResponse(Skill skill, String categoryName) {
        List<SkillPrerequisite> prereqs = prerequisiteRepository.findByIdSkillId(skill.getId());
        List<SkillResponse> prereqResponses = new ArrayList<>();
        for (SkillPrerequisite p : prereqs) {
            skillRepository.findById(p.getPrerequisiteSkillId()).ifPresent(pr -> {
                String catName = categoryRepository.findById(pr.getCategoryId())
                        .map(SkillCategory::getName).orElse("General");
                prereqResponses.add(new SkillResponse(
                        pr.getId(), pr.getName(), pr.getDescription(), pr.getCategoryId(), catName, pr.isGlobal(), pr.getTags(), List.of()
                ));
            });
        }
        return SkillResponse.from(skill, categoryName, prereqResponses);
    }
}
