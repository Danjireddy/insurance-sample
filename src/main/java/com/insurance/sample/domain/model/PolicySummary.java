package com.insurance.sample.domain.model;

import java.math.BigDecimal;
import java.util.Map;

public record PolicySummary(
        Map<PolicyStatus, Long> countByStatus,
        Map<LineOfBusiness, BigDecimal> totalPremiumByLineOfBusiness,
        long expiringSoonCount,
        long totalActivePolicies,
        BigDecimal totalPremium
) {}
