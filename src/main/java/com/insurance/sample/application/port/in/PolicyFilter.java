package com.insurance.sample.application.port.in;

import com.insurance.sample.domain.model.LineOfBusiness;
import com.insurance.sample.domain.model.PolicyStatus;

import java.time.LocalDate;

public record PolicyFilter(
        PolicyStatus status,
        LineOfBusiness lineOfBusiness,
        String region,
        LocalDate effectiveDateFrom,
        LocalDate effectiveDateTo,
        String search
) {
    public static PolicyFilter empty() {
        return new PolicyFilter(null, null, null, null, null, null);
    }
}
