package com.insurance.sample.api.controller;

import com.insurance.sample.api.dto.*;
import com.insurance.sample.api.mapper.PolicyApiMapper;
import com.insurance.sample.application.port.in.PolicyFilter;
import com.insurance.sample.application.port.in.PolicyUseCase;
import com.insurance.sample.domain.model.LineOfBusiness;
import com.insurance.sample.domain.model.PolicyStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Policies", description = "Policy management operations")
public class PolicyController {

    private final PolicyUseCase policyUseCase;
    private final PolicyApiMapper mapper;

    @GetMapping
    @Operation(summary = "List policies with pagination, sorting and filtering")
    public ResponseEntity<PolicyPageResponse> listPolicies(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) LineOfBusiness lineOfBusiness,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateTo,
            @RequestParam(required = false) String search
    ) {
        PolicyFilter filter = new PolicyFilter(status, lineOfBusiness, region, effectiveDateFrom, effectiveDateTo, search);
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(mapper.toPageResponse(policyUseCase.listPolicies(filter, pageable)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Aggregated statistics")
    public ResponseEntity<PolicySummaryResponse> getSummary() {
        return ResponseEntity.ok(mapper.toSummaryResponse(policyUseCase.getSummary()));
    }

    @PatchMapping("/flag")
    @Operation(summary = "Bulk flag policies for review")
    public ResponseEntity<BulkFlagResponse> flagPolicies(@Valid @RequestBody BulkFlagRequest request) {
        return ResponseEntity.ok(mapper.toBulkFlagResponse(
                policyUseCase.flagPoliciesForReview(request.policyIds())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single policy by UUID")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(policyUseCase.getPolicyById(id)));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sort) {
        try {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            return PageRequest.of(page, size, Sort.by(direction, field));
        } catch (Exception e) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }
}
