package com.insurance.sample.application.port.in;

import com.insurance.sample.domain.model.Policy;
import com.insurance.sample.domain.model.PolicySummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PolicyUseCase {

    Page<Policy> listPolicies(PolicyFilter filter, Pageable pageable);

    Policy getPolicyById(UUID id);

    BulkFlagResult flagPoliciesForReview(List<UUID> policyIds);

    PolicySummary getSummary();

    record BulkFlagResult(int flaggedCount, List<UUID> flaggedIds, List<UUID> notFoundIds) {}
}
