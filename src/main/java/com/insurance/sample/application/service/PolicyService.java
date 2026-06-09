package com.insurance.sample.application.service;

import com.insurance.sample.application.port.in.PolicyFilter;
import com.insurance.sample.application.port.in.PolicyUseCase;
import com.insurance.sample.application.port.out.PolicyEventPublisher;
import com.insurance.sample.application.port.out.PolicyRepository;
import com.insurance.sample.domain.event.PolicyFlaggedEvent;
import com.insurance.sample.domain.model.Policy;
import com.insurance.sample.domain.model.PolicySummary;
import com.insurance.sample.infrastructure.exception.PolicyNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyService implements PolicyUseCase {

    private final PolicyRepository policyRepository;
    private final PolicyEventPublisher eventPublisher;

    @Override
    @Cacheable(value = "policy-list", key = "#filter.toString() + '_' + #pageable.toString()")
    public Page<Policy> listPolicies(PolicyFilter filter, Pageable pageable) {
        log.debug("Listing policies with filter={}, pageable={}", filter, pageable);
        return policyRepository.findAll(filter, pageable);
    }

    @Override
    @Cacheable(value = "policy-detail", key = "#id")
    public Policy getPolicyById(UUID id) {
        log.debug("Fetching policy id={}", id);
        return policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "policy-list", allEntries = true),
            @CacheEvict(value = "policy-summary", allEntries = true)
    })
    public BulkFlagResult flagPoliciesForReview(List<UUID> policyIds) {
        log.info("Bulk flagging {} policy IDs for review", policyIds.size());

        List<Policy> found = policyRepository.findAllById(policyIds);
        List<UUID> foundIds = found.stream().map(Policy::getId).toList();
        List<UUID> notFoundIds = policyIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        List<Policy> toFlag = new ArrayList<>();
        for (Policy policy : found) {
            if (!policy.isFlaggedForReview()) {
                policy.flagForReview();
                toFlag.add(policy);
            }
        }

        if (!toFlag.isEmpty()) {
            policyRepository.saveAll(toFlag);
            toFlag.forEach(p -> {
                eventPublisher.publishFlagged(PolicyFlaggedEvent.of(p.getId(), p.getPolicyNumber()));
                // Evict individual detail cache entries
            });
        }

        log.info("Flagged {} policies, {} not found", toFlag.size(), notFoundIds.size());
        return new BulkFlagResult(toFlag.size(), toFlag.stream().map(Policy::getId).toList(), notFoundIds);
    }

    @Override
    @Cacheable(value = "policy-summary")
    public PolicySummary getSummary() {
        log.debug("Computing policy summary");
        return policyRepository.computeSummary();
    }
}
