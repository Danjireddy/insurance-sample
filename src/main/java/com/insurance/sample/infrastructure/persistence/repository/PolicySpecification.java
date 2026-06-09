package com.insurance.sample.infrastructure.persistence.repository;

import com.insurance.sample.application.port.in.PolicyFilter;
import com.insurance.sample.infrastructure.persistence.entity.PolicyEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PolicySpecification {

    private PolicySpecification() {}

    public static Specification<PolicyEntity> from(PolicyFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            if (filter.lineOfBusiness() != null) {
                predicates.add(cb.equal(root.get("lineOfBusiness"), filter.lineOfBusiness()));
            }

            if (filter.region() != null && !filter.region().isBlank()) {
                predicates.add(cb.equal(root.get("region"), filter.region()));
            }

            if (filter.effectiveDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("effectiveDate"), filter.effectiveDateFrom()));
            }

            if (filter.effectiveDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("effectiveDate"), filter.effectiveDateTo()));
            }

            if (filter.search() != null && !filter.search().isBlank()) {
                String pattern = "%" + filter.search().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("policyNumber")), pattern),
                        cb.like(cb.lower(root.get("policyholderName")), pattern),
                        cb.like(cb.lower(root.get("underwriter")), pattern)
                ));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
