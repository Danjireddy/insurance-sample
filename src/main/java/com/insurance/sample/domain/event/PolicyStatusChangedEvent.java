package com.insurance.sample.domain.event;

import com.insurance.sample.domain.model.PolicyStatus;

import java.time.Instant;
import java.util.UUID;

public record PolicyStatusChangedEvent(
        UUID eventId,
        UUID policyId,
        PolicyStatus previousStatus,
        PolicyStatus newStatus,
        Instant changedAt
) {
    public static PolicyStatusChangedEvent of(UUID policyId, PolicyStatus from, PolicyStatus to) {
        return new PolicyStatusChangedEvent(UUID.randomUUID(), policyId, from, to, Instant.now());
    }
}
