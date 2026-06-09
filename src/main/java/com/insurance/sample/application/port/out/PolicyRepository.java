package com.insurance.sample.application.port.out;

import com.insurance.sample.application.port.in.PolicyFilter;
import com.insurance.sample.domain.model.Policy;
import com.insurance.sample.domain.model.PolicySummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository {

    Page<Policy> findAll(PolicyFilter filter, Pageable pageable);

    Optional<Policy> findById(UUID id);

    List<Policy> findAllById(List<UUID> ids);

    List<Policy> saveAll(List<Policy> policies);

    PolicySummary computeSummary();
}
