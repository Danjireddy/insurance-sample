package com.insurance.sample.application.service;

import com.insurance.sample.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyDomainTest {

    private Policy buildPolicy(PolicyStatus status, LocalDate expiryDate) {
        return new Policy(
                UUID.randomUUID(), "POL-TEST01", "Test Corp", LineOfBusiness.Property,
                status, new BigDecimal("100000"), "SGD",
                LocalDate.now().minusMonths(6), expiryDate,
                "Singapore", "Test Underwriter", false,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void flagForReview_shouldSetFlaggedAndUpdateTimestamp() {
        Policy policy = buildPolicy(PolicyStatus.Active, LocalDate.now().plusMonths(6));
        assertThat(policy.isFlaggedForReview()).isFalse();

        policy.flagForReview();

        assertThat(policy.isFlaggedForReview()).isTrue();
        assertThat(policy.getUpdatedAt()).isNotNull();
    }

    @Test
    void changeStatus_shouldUpdateStatusAndTimestamp() {
        Policy policy = buildPolicy(PolicyStatus.Pending, LocalDate.now().plusMonths(3));

        policy.changeStatus(PolicyStatus.Active);

        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.Active);
    }

    @Test
    void isExpiringSoon_withinThreshold_shouldReturnTrue() {
        Policy policy = buildPolicy(PolicyStatus.Active, LocalDate.now().plusDays(15));
        assertThat(policy.isExpiringSoon(30)).isTrue();
    }

    @Test
    void isExpiringSoon_beyondThreshold_shouldReturnFalse() {
        Policy policy = buildPolicy(PolicyStatus.Active, LocalDate.now().plusDays(60));
        assertThat(policy.isExpiringSoon(30)).isFalse();
    }

    @Test
    void isExpiringSoon_expiredPolicy_shouldReturnFalse() {
        Policy policy = buildPolicy(PolicyStatus.Expired, LocalDate.now().minusDays(1));
        assertThat(policy.isExpiringSoon(30)).isFalse();
    }
}
