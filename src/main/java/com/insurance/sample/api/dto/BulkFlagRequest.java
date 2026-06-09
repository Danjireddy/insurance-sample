package com.insurance.sample.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record BulkFlagRequest(
        @NotEmpty(message = "policyIds must not be empty")
        @Size(max = 100, message = "Cannot flag more than 100 policies at once")
        List<UUID> policyIds
) {}
