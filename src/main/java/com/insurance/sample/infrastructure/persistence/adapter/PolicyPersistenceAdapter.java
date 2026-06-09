package com.insurance.sample.infrastructure.persistence.adapter;

import com.insurance.sample.application.port.in.PolicyFilter;
import com.insurance.sample.application.port.out.PolicyRepository;
import com.insurance.sample.domain.model.*;
import com.insurance.sample.infrastructure.persistence.entity.PolicyEntity;
import com.insurance.sample.infrastructure.persistence.repository.PolicyJpaRepository;
import com.insurance.sample.infrastructure.persistence.repository.PolicySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class PolicyPersistenceAdapter implements PolicyRepository {

    private static final int EXPIRING_SOON_DAYS = 30;

    private final PolicyJpaRepository jpaRepository;

    @Override
    public Page<Policy> findAll(PolicyFilter filter, Pageable pageable) {
        return jpaRepository
                .findAll(PolicySpecification.from(filter), pageable)
                .map(this::toDomain);
    }

    @Override
    public Optional<Policy> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Policy> findAllById(List<UUID> ids) {
        return jpaRepository.findAllByIdIn(ids).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Policy> saveAll(List<Policy> policies) {
        List<UUID> ids = policies.stream().map(Policy::getId).toList();
        List<PolicyEntity> entities = jpaRepository.findAllByIdIn(ids);

        // Apply domain state back to entities
        Map<UUID, Policy> domainMap = new HashMap<>();
        policies.forEach(p -> domainMap.put(p.getId(), p));

        entities.forEach(entity -> {
            Policy domain = domainMap.get(entity.getId());
            if (domain != null) {
                entity.setStatus(domain.getStatus());
                entity.setFlaggedForReview(domain.isFlaggedForReview());
                entity.setUpdatedAt(domain.getUpdatedAt());
            }
        });

        return jpaRepository.saveAll(entities).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PolicySummary computeSummary() {
        // Count by status
        Map<PolicyStatus, Long> countByStatus = new EnumMap<>(PolicyStatus.class);
        jpaRepository.countByStatus().forEach(row -> {
            PolicyStatus status = (PolicyStatus) row[0];
            Long count = (Long) row[1];
            countByStatus.put(status, count);
        });

        // Premium by LOB
        Map<LineOfBusiness, BigDecimal> premiumByLob = new EnumMap<>(LineOfBusiness.class);
        jpaRepository.sumPremiumByLineOfBusiness().forEach(row -> {
            LineOfBusiness lob = (LineOfBusiness) row[0];
            BigDecimal total = (BigDecimal) row[1];
            premiumByLob.put(lob, total);
        });

        long activePolicies = jpaRepository.countByStatusEquals(PolicyStatus.Active);
        BigDecimal totalPremium = jpaRepository.sumPremiumByStatus(PolicyStatus.Active);
        if (totalPremium == null) totalPremium = BigDecimal.ZERO;

        long expiringSoon = jpaRepository.countExpiringSoon(
                LocalDate.now().plusDays(EXPIRING_SOON_DAYS),
                List.of(PolicyStatus.Expired, PolicyStatus.Cancelled));

        return new PolicySummary(countByStatus, premiumByLob, expiringSoon, activePolicies, totalPremium);
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    private Policy toDomain(PolicyEntity e) {
        return new Policy(
                e.getId(),
                e.getPolicyNumber(),
                e.getPolicyholderName(),
                e.getLineOfBusiness(),
                e.getStatus(),
                e.getPremiumAmount(),
                e.getCurrency(),
                e.getEffectiveDate(),
                e.getExpiryDate(),
                e.getRegion(),
                e.getUnderwriter(),
                e.isFlaggedForReview(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
