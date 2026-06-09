package com.insurance.sample.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PolicyFlaggedEvent(
        UUID eventId,
        UUID policyId,
        String policyNumber,
        Instant flaggedAt,
        String reason
) {
    public static PolicyFlaggedEvent of(UUID policyId, String policyNumber) {
        return new PolicyFlaggedEvent(
                UUID.randomUUID(),
                policyId,
                policyNumber,
                Instant.now(),
                "MANUAL_REVIEW"
        );
    }
}
