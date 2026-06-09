package com.insurance.sample.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Policy aggregate root — pure domain object, no framework dependencies.
 */
public class Policy {

    private final UUID id;
    private final String policyNumber;
    private final String policyholderName;
    private final LineOfBusiness lineOfBusiness;
    private PolicyStatus status;
    private final BigDecimal premiumAmount;
    private final String currency;
    private final LocalDate effectiveDate;
    private final LocalDate expiryDate;
    private final String region;
    private final String underwriter;
    private boolean flaggedForReview;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Policy(UUID id, String policyNumber, String policyholderName,
                  LineOfBusiness lineOfBusiness, PolicyStatus status,
                  BigDecimal premiumAmount, String currency,
                  LocalDate effectiveDate, LocalDate expiryDate,
                  String region, String underwriter,
                  boolean flaggedForReview,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.policyNumber = policyNumber;
        this.policyholderName = policyholderName;
        this.lineOfBusiness = lineOfBusiness;
        this.status = status;
        this.premiumAmount = premiumAmount;
        this.currency = currency;
        this.effectiveDate = effectiveDate;
        this.expiryDate = expiryDate;
        this.region = region;
        this.underwriter = underwriter;
        this.flaggedForReview = flaggedForReview;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Domain behaviours ──────────────────────────────────────────────────

    public void flagForReview() {
        this.flaggedForReview = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(PolicyStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExpiringSoon(int withinDays) {
        if (status == PolicyStatus.Expired || status == PolicyStatus.Cancelled) {
            return false;
        }
        LocalDate threshold = LocalDate.now().plusDays(withinDays);
        return expiryDate != null && !expiryDate.isAfter(threshold) && !expiryDate.isBefore(LocalDate.now());
    }

    // ── Getters ────────────────────────────────────────────────────────────

    public UUID getId()                        { return id; }
    public String getPolicyNumber()            { return policyNumber; }
    public String getPolicyholderName()        { return policyholderName; }
    public LineOfBusiness getLineOfBusiness()  { return lineOfBusiness; }
    public PolicyStatus getStatus()            { return status; }
    public BigDecimal getPremiumAmount()       { return premiumAmount; }
    public String getCurrency()                { return currency; }
    public LocalDate getEffectiveDate()        { return effectiveDate; }
    public LocalDate getExpiryDate()           { return expiryDate; }
    public String getRegion()                  { return region; }
    public String getUnderwriter()             { return underwriter; }
    public boolean isFlaggedForReview()        { return flaggedForReview; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public LocalDateTime getUpdatedAt()        { return updatedAt; }
}
