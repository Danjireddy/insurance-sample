package com.insurance.sample.infrastructure.persistence;

import com.insurance.sample.application.port.in.PolicyFilter;
import com.insurance.sample.application.port.out.PolicyRepository;
import com.insurance.sample.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PolicyPersistenceAdapterTest {

    @Autowired PolicyRepository policyRepository;

    @Test
    void seedData_shouldContainAtLeast200Records() {
        Page<Policy> all = policyRepository.findAll(PolicyFilter.empty(), PageRequest.of(0, 1));
        assertThat(all.getTotalElements()).isGreaterThanOrEqualTo(200);
    }

    @Test
    void findAll_noFilter_shouldReturnPaginatedResults() {
        Page<Policy> page = policyRepository.findAll(PolicyFilter.empty(), PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalPages()).isGreaterThan(1);
    }

    @Test
    void findAll_filterByStatus_shouldReturnOnlyMatchingRecords() {
        PolicyFilter filter = new PolicyFilter(PolicyStatus.Active, null, null, null, null, null);
        Page<Policy> page = policyRepository.findAll(filter, PageRequest.of(0, 100));
        assertThat(page.getContent()).allMatch(p -> p.getStatus() == PolicyStatus.Active);
    }

    @Test
    void findAll_filterByLineOfBusiness_shouldReturnOnlyMatchingRecords() {
        PolicyFilter filter = new PolicyFilter(null, LineOfBusiness.Marine, null, null, null, null);
        Page<Policy> page = policyRepository.findAll(filter, PageRequest.of(0, 100));
        assertThat(page.getContent()).allMatch(p -> p.getLineOfBusiness() == LineOfBusiness.Marine);
    }

    @Test
    void findAll_filterByRegion_shouldReturnOnlyMatchingRecords() {
        PolicyFilter filter = new PolicyFilter(null, null, "Singapore", null, null, null);
        Page<Policy> page = policyRepository.findAll(filter, PageRequest.of(0, 50));
        assertThat(page.getContent()).allMatch(p -> "Singapore".equals(p.getRegion()));
    }

    @Test
    void findAll_searchByPolicyholderName_shouldReturnMatches() {
        PolicyFilter filter = new PolicyFilter(null, null, null, null, null, "DBS");
        Page<Policy> page = policyRepository.findAll(filter, PageRequest.of(0, 10));
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent()).anyMatch(p ->
                p.getPolicyholderName().toLowerCase().contains("dbs"));
    }

    @Test
    void findById_existingPolicy_shouldReturnIt() {
        Page<Policy> first = policyRepository.findAll(PolicyFilter.empty(), PageRequest.of(0, 1));
        UUID id = first.getContent().get(0).getId();

        Optional<Policy> found = policyRepository.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
    }

    @Test
    void findById_unknownId_shouldReturnEmpty() {
        Optional<Policy> result = policyRepository.findById(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    @Test
    void saveAll_shouldPersistFlagChange() {
        Page<Policy> page = policyRepository.findAll(PolicyFilter.empty(), PageRequest.of(0, 1));
        Policy policy = page.getContent().get(0);
        assertThat(policy.isFlaggedForReview()).isFalse();

        policy.flagForReview();
        policyRepository.saveAll(List.of(policy));

        Optional<Policy> reloaded = policyRepository.findById(policy.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().isFlaggedForReview()).isTrue();
    }

    @Test
    void computeSummary_shouldReturnNonNullSummary() {
        PolicySummary summary = policyRepository.computeSummary();

        assertThat(summary).isNotNull();
        assertThat(summary.countByStatus()).isNotEmpty();
        assertThat(summary.totalPremiumByLineOfBusiness()).isNotEmpty();
        assertThat(summary.totalActivePolicies()).isGreaterThan(0);
        assertThat(summary.totalPremium()).isPositive();
    }

    @Test
    void findAll_sortByPremiumDesc_shouldReturnOrderedResults() {
        Page<Policy> page = policyRepository.findAll(PolicyFilter.empty(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "premiumAmount")));
        List<Policy> policies = page.getContent();
        for (int i = 0; i < policies.size() - 1; i++) {
            assertThat(policies.get(i).getPremiumAmount())
                    .isGreaterThanOrEqualTo(policies.get(i + 1).getPremiumAmount());
        }
    }
}
