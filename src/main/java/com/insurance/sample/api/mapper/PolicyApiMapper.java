package com.insurance.sample.api.mapper;

import com.insurance.sample.api.dto.*;
import com.insurance.sample.application.port.in.PolicyUseCase;
import com.insurance.sample.domain.model.Policy;
import com.insurance.sample.domain.model.PolicySummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PolicyApiMapper {

    PolicyResponse toResponse(Policy policy);

    default PolicyPageResponse toPageResponse(Page<Policy> page) {
        List<PolicyResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PolicyPageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    default BulkFlagResponse toBulkFlagResponse(PolicyUseCase.BulkFlagResult result) {
        return new BulkFlagResponse(result.flaggedCount(), result.flaggedIds(), result.notFoundIds());
    }

    default PolicySummaryResponse toSummaryResponse(PolicySummary summary) {
        return new PolicySummaryResponse(
                summary.countByStatus(),
                summary.totalPremiumByLineOfBusiness(),
                summary.expiringSoonCount(),
                summary.totalActivePolicies(),
                summary.totalPremium()
        );
    }
}
