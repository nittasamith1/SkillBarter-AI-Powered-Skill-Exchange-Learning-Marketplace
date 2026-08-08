package com.skillbarter.skill.repository;

import com.skillbarter.skill.entity.SkillPrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillPrerequisiteRepository extends JpaRepository<SkillPrerequisite, SkillPrerequisite.SkillPrerequisiteId> {

    List<SkillPrerequisite> findByIdSkillId(UUID skillId);

    void deleteByIdSkillId(UUID skillId);
}
