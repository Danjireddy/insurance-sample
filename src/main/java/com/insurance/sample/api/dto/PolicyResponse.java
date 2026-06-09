package com.insurance.sample.api.dto;

import com.insurance.sample.domain.model.LineOfBusiness;
import com.insurance.sample.domain.model.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PolicyResponse(
        UUID id,
        String policyNumber,
        String policyholderName,
        LineOfBusiness lineOfBusiness,
        PolicyStatus status,
        BigDecimal premiumAmount,
        String currency,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        String region,
        String underwriter,
        boolean flaggedForReview,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
