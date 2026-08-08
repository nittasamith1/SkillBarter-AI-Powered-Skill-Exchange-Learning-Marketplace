package com.skillbarter.skill.repository;

import com.skillbarter.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {

    List<Skill> findByCategoryId(UUID categoryId);

    List<Skill> findByIsGlobalTrue();

    @Query("SELECT s FROM Skill s WHERE s.isGlobal = true AND " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Skill> searchByName(@Param("search") String search);

    @Query("SELECT s FROM Skill s WHERE s.isGlobal = true AND s.categoryId = :categoryId AND " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Skill> searchByNameAndCategory(@Param("search") String search, @Param("categoryId") UUID categoryId);

    @Query("SELECT s FROM Skill s WHERE s.isGlobal = true AND s.categoryId = :categoryId")
    List<Skill> findByCategoryIdAndGlobal(@Param("categoryId") UUID categoryId);

    boolean existsByNameAndCategoryId(String name, UUID categoryId);
}
