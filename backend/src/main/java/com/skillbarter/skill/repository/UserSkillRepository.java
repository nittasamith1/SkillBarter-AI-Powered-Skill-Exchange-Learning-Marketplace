package com.skillbarter.skill.repository;

import com.skillbarter.skill.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {

    List<UserSkill> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    Optional<UserSkill> findByUserIdAndSkillId(UUID userId, UUID skillId);

    boolean existsByUserIdAndSkillId(UUID userId, UUID skillId);

    List<UserSkill> findByUserIdAndTenantIdAndCanTeachTrue(UUID userId, UUID tenantId);

    List<UserSkill> findByUserIdAndTenantIdAndWantToLearnTrue(UUID userId, UUID tenantId);

    /** Count how many users (in any tenant) can teach a given skill */
    @Query("SELECT COUNT(us) FROM UserSkill us WHERE us.skillId = :skillId AND us.canTeach = true")
    long countTeachersBySkillId(@Param("skillId") UUID skillId);

    /** Count how many users want to learn a given skill */
    @Query("SELECT COUNT(us) FROM UserSkill us WHERE us.skillId = :skillId AND us.wantToLearn = true")
    long countLearnersBySkillId(@Param("skillId") UUID skillId);

    /** All users in a tenant who can teach a given skill */
    @Query("SELECT us FROM UserSkill us WHERE us.tenantId = :tenantId AND us.skillId = :skillId AND us.canTeach = true")
    List<UserSkill> findTeachersInTenant(@Param("tenantId") UUID tenantId, @Param("skillId") UUID skillId);

    /** Find users in tenant by skill name (for search) */
    @Query("SELECT us FROM UserSkill us WHERE us.tenantId = :tenantId AND us.skillId IN :skillIds")
    List<UserSkill> findByTenantIdAndSkillIdIn(@Param("tenantId") UUID tenantId, @Param("skillIds") List<UUID> skillIds);
}
