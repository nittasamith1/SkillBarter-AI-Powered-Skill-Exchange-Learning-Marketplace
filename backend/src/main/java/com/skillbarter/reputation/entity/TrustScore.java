package com.skillbarter.reputation.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trust_scores")
public class TrustScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(updatable = false, nullable = false, length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private UUID userId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "tenant_id", nullable = false, length = 36)
    private UUID tenantId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score = new BigDecimal("100.00");

    @Column(name = "rating_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal ratingScore = new BigDecimal("100.00");

    @Column(name = "completion_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal completionScore = new BigDecimal("100.00");

    @Column(name = "reliability_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal reliabilityScore = new BigDecimal("100.00");

    @Column(name = "response_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal responseScore = new BigDecimal("100.00");

    @Column(name = "cancellation_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal cancellationScore = new BigDecimal("100.00");

    @CreationTimestamp
    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    public TrustScore() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public BigDecimal getRatingScore() { return ratingScore; }
    public void setRatingScore(BigDecimal ratingScore) { this.ratingScore = ratingScore; }

    public BigDecimal getCompletionScore() { return completionScore; }
    public void setCompletionScore(BigDecimal completionScore) { this.completionScore = completionScore; }

    public BigDecimal getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(BigDecimal reliabilityScore) { this.reliabilityScore = reliabilityScore; }

    public BigDecimal getResponseScore() { return responseScore; }
    public void setResponseScore(BigDecimal responseScore) { this.responseScore = responseScore; }

    public BigDecimal getCancellationScore() { return cancellationScore; }
    public void setCancellationScore(BigDecimal cancellationScore) { this.cancellationScore = cancellationScore; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
