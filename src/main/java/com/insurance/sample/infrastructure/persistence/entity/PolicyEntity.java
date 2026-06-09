package com.insurance.sample.infrastructure.persistence.entity;

import com.insurance.sample.domain.model.LineOfBusiness;
import com.insurance.sample.domain.model.PolicyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "policies",
        schema = "my_sql",
        indexes = {
                @Index(name = "idx_policies_status", columnList = "status"),
                @Index(name = "idx_policies_lob", columnList = "line_of_business"),
                @Index(name = "idx_policies_region", columnList = "region"),
                @Index(name = "idx_policies_effective_date", columnList = "effective_date"),
                @Index(name = "idx_policies_expiry_date", columnList = "expiry_date"),
                @Index(name = "idx_policies_flagged", columnList = "flagged_for_review"),
                @Index(name = "idx_policies_policy_number", columnList = "policy_number", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "policy_number", nullable = false, unique = true, length = 20)
    private String policyNumber;

    @Column(name = "policyholder_name", nullable = false, length = 200)
    private String policyholderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_of_business", nullable = false, length = 20)
    private LineOfBusiness lineOfBusiness;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PolicyStatus status;

    @Column(name = "premium_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "underwriter", nullable = false, length = 100)
    private String underwriter;

    @Column(name = "flagged_for_review", nullable = false)
    @Builder.Default
    private boolean flaggedForReview = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
