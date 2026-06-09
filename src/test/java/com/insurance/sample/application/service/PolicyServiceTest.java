package com.insurance.sample.application.service;

import com.insurance.sample.application.port.in.PolicyFilter;
import com.insurance.sample.application.port.in.PolicyUseCase;
import com.insurance.sample.application.port.out.PolicyEventPublisher;
import com.insurance.sample.application.port.out.PolicyRepository;
import com.insurance.sample.domain.model.*;
import com.insurance.sample.infrastructure.exception.PolicyNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock PolicyRepository policyRepository;
    @Mock PolicyEventPublisher eventPublisher;
    @InjectMocks PolicyService policyService;

    private Policy buildPolicy(UUID id, boolean flagged) {
        return new Policy(id, "POL-" + id.toString().substring(0, 6), "Acme Corp",
                LineOfBusiness.Property, PolicyStatus.Active,
                new BigDecimal("50000"), "SGD",
                LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(11),
                "Singapore", "Underwriter A", flagged,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void listPolicies_shouldDelegateToRepository() {
        List<Policy> policies = List.of(buildPolicy(UUID.randomUUID(), false));
        Page<Policy> page = new PageImpl<>(policies);
        when(policyRepository.findAll(any(), any())).thenReturn(page);

        Page<Policy> result = policyService.listPolicies(PolicyFilter.empty(), PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        verify(policyRepository).findAll(any(), any());
    }

    @Test
    void getPolicyById_found_shouldReturnPolicy() {
        UUID id = UUID.randomUUID();
        Policy policy = buildPolicy(id, false);
        when(policyRepository.findById(id)).thenReturn(Optional.of(policy));

        Policy result = policyService.getPolicyById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void getPolicyById_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(policyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.getPolicyById(id))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void flagPoliciesForReview_shouldFlagAndPublishEvents() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Policy p1 = buildPolicy(id1, false);
        Policy p2 = buildPolicy(id2, false);

        when(policyRepository.findAllById(List.of(id1, id2))).thenReturn(List.of(p1, p2));
        when(policyRepository.saveAll(any())).thenReturn(List.of(p1, p2));

        PolicyUseCase.BulkFlagResult result = policyService.flagPoliciesForReview(List.of(id1, id2));

        assertThat(result.flaggedCount()).isEqualTo(2);
        assertThat(result.notFoundIds()).isEmpty();
        verify(eventPublisher, times(2)).publishFlagged(any());
    }

    @Test
    void flagPoliciesForReview_missingIds_shouldReportNotFound() {
        UUID existingId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        Policy policy = buildPolicy(existingId, false);

        when(policyRepository.findAllById(any())).thenReturn(List.of(policy));
        when(policyRepository.saveAll(any())).thenReturn(List.of(policy));

        PolicyUseCase.BulkFlagResult result =
                policyService.flagPoliciesForReview(List.of(existingId, missingId));

        assertThat(result.notFoundIds()).containsExactly(missingId);
    }

    @Test
    void flagPoliciesForReview_alreadyFlagged_shouldNotRepublish() {
        UUID id = UUID.randomUUID();
        Policy alreadyFlagged = buildPolicy(id, true);

        when(policyRepository.findAllById(List.of(id))).thenReturn(List.of(alreadyFlagged));

        PolicyUseCase.BulkFlagResult result = policyService.flagPoliciesForReview(List.of(id));

        assertThat(result.flaggedCount()).isZero();
        verify(eventPublisher, never()).publishFlagged(any());
    }

    @Test
    void getSummary_shouldDelegateToRepository() {
        PolicySummary summary = new PolicySummary(Map.of(), Map.of(), 0, 0, BigDecimal.ZERO);
        when(policyRepository.computeSummary()).thenReturn(summary);

        PolicySummary result = policyService.getSummary();

        assertThat(result).isEqualTo(summary);
    }
}
