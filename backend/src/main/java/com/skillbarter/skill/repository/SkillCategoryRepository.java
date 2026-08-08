package com.skillbarter.skill.repository;

import com.skillbarter.skill.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, UUID> {

    List<SkillCategory> findByParentIdIsNull();

    List<SkillCategory> findByParentId(UUID parentId);

    @Query("SELECT sc FROM SkillCategory sc ORDER BY sc.parentId ASC NULLS FIRST, sc.name ASC")
    List<SkillCategory> findAllOrdered();
}
