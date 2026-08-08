package com.skillbarter.skill.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a prerequisite relationship between two skills.
 * A skill can have multiple prerequisites.
 */
@Entity
@Table(name = "skill_prerequisites")
public class SkillPrerequisite {

    @EmbeddedId
    private SkillPrerequisiteId id = new SkillPrerequisiteId();

    public SkillPrerequisite() {}

    public SkillPrerequisite(UUID skillId, UUID prerequisiteSkillId) {
        this.id = new SkillPrerequisiteId(skillId, prerequisiteSkillId);
    }

    public UUID getSkillId() { return id.skillId; }
    public UUID getPrerequisiteSkillId() { return id.prerequisiteSkillId; }
    public SkillPrerequisiteId getId() { return id; }
    public void setId(SkillPrerequisiteId id) { this.id = id; }

    @Embeddable
    public static class SkillPrerequisiteId implements Serializable {

        @JdbcTypeCode(SqlTypes.CHAR)
        @Column(name = "skill_id", length = 36)
        private UUID skillId;

        @JdbcTypeCode(SqlTypes.CHAR)
        @Column(name = "prerequisite_skill_id", length = 36)
        private UUID prerequisiteSkillId;

        public SkillPrerequisiteId() {}

        public SkillPrerequisiteId(UUID skillId, UUID prerequisiteSkillId) {
            this.skillId = skillId;
            this.prerequisiteSkillId = prerequisiteSkillId;
        }

        public UUID getSkillId() { return skillId; }
        public UUID getPrerequisiteSkillId() { return prerequisiteSkillId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SkillPrerequisiteId that)) return false;
            return Objects.equals(skillId, that.skillId) &&
                   Objects.equals(prerequisiteSkillId, that.prerequisiteSkillId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(skillId, prerequisiteSkillId);
        }
    }
}
