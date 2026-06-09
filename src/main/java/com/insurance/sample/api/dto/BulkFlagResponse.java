package com.insurance.sample.api.dto;

import java.util.List;
import java.util.UUID;

public record BulkFlagResponse(
        int flaggedCount,
        List<UUID> flaggedIds,
        List<UUID> notFoundIds
) {}
