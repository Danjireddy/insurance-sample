package com.insurance.sample.api.dto;

import java.util.List;

public record PolicyPageResponse(
        List<PolicyResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {}
