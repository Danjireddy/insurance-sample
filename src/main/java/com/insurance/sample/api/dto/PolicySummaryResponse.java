package com.insurance.sample.api.dto;

import com.insurance.sample.domain.model.LineOfBusiness;
import com.insurance.sample.domain.model.PolicyStatus;

import java.math.BigDecimal;
import java.util.Map;

public record PolicySummaryResponse(
        Map<PolicyStatus, Long> countByStatus,
        Map<LineOfBusiness, BigDecimal> totalPremiumByLineOfBusiness,
        long expiringSoonCount,
        long totalActivePolicies,
        BigDecimal totalPremium
) {}
