package com.skillbarter.marketplace.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * A peer-to-peer skill exchange request.
 * Requester offers to teach one skill in exchange for learning another from the receiver.
 */
@Entity
@Table(name = "exchange_requests")
public class ExchangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(updatable = false, nullable = false, length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "tenant_id", nullable = false, length = 36)
    private UUID tenantId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "requester_id", nullable = false, length = 36)
    private UUID requesterId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "receiver_id", nullable = false, length = 36)
    private UUID receiverId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "offered_skill_id", nullable = false, length = 36)
    private UUID offeredSkillId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "wanted_skill_id", nullable = false, length = 36)
    private UUID wantedSkillId;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeStatus status = ExchangeStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum ExchangeStatus {
        PENDING, ACCEPTED, REJECTED, CANCELLED
    }

    public ExchangeRequest() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getRequesterId() { return requesterId; }
    public void setRequesterId(UUID requesterId) { this.requesterId = requesterId; }

    public UUID getReceiverId() { return receiverId; }
    public void setReceiverId(UUID receiverId) { this.receiverId = receiverId; }

    public UUID getOfferedSkillId() { return offeredSkillId; }
    public void setOfferedSkillId(UUID offeredSkillId) { this.offeredSkillId = offeredSkillId; }

    public UUID getWantedSkillId() { return wantedSkillId; }
    public void setWantedSkillId(UUID wantedSkillId) { this.wantedSkillId = wantedSkillId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ExchangeStatus getStatus() { return status; }
    public void setStatus(ExchangeStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
