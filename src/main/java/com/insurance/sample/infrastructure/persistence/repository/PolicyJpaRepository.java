package com.insurance.sample.infrastructure.persistence.repository;

import com.insurance.sample.domain.model.PolicyStatus;
import com.insurance.sample.infrastructure.persistence.entity.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PolicyJpaRepository
        extends JpaRepository<PolicyEntity, UUID>, JpaSpecificationExecutor<PolicyEntity> {

    List<PolicyEntity> findAllByIdIn(List<UUID> ids);

    @Query("SELECT p.status, COUNT(p) FROM PolicyEntity p GROUP BY p.status")
    List<Object[]> countByStatus();

    @Query("SELECT p.lineOfBusiness, SUM(p.premiumAmount) FROM PolicyEntity p GROUP BY p.lineOfBusiness")
    List<Object[]> sumPremiumByLineOfBusiness();

    // Use named parameter to pass enum value — avoids JPQL string-literal enum pitfall
    @Query("SELECT COUNT(p) FROM PolicyEntity p WHERE p.status = :status")
    long countByStatusEquals(PolicyStatus status);

    @Query("SELECT COALESCE(SUM(p.premiumAmount), 0) FROM PolicyEntity p WHERE p.status = :status")
    BigDecimal sumPremiumByStatus(PolicyStatus status);

    @Query("SELECT COUNT(p) FROM PolicyEntity p WHERE p.status NOT IN :excludedStatuses AND p.expiryDate <= :threshold AND p.expiryDate >= CURRENT_DATE")
    long countExpiringSoon(LocalDate threshold, List<PolicyStatus> excludedStatuses);
}
