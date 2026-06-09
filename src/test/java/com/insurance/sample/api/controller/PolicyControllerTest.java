package com.insurance.sample.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.sample.api.dto.BulkFlagRequest;
import com.insurance.sample.application.port.in.PolicyUseCase;
import com.insurance.sample.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PolicyControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean PolicyUseCase policyUseCase;

    private Policy samplePolicy() {
        return new Policy(
                UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001"),
                "POL-000001", "Test Corp",
                LineOfBusiness.Property, PolicyStatus.Active,
                new BigDecimal("50000"), "SGD",
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1),
                "Singapore", "Underwriter A", false,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void listPolicies_shouldReturn200WithPage() throws Exception {
        when(policyUseCase.listPolicies(any(), any()))
                .thenReturn(new PageImpl<>(List.of(samplePolicy()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].policyNumber").value("POL-000001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getPolicyById_found_shouldReturn200() throws Exception {
        UUID id = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
        when(policyUseCase.getPolicyById(id)).thenReturn(samplePolicy());

        mockMvc.perform(get("/api/v1/policies/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("Active"));
    }

    @Test
    void getPolicyById_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(policyUseCase.getPolicyById(id))
                .thenThrow(new com.insurance.sample.infrastructure.exception.PolicyNotFoundException(id));

        mockMvc.perform(get("/api/v1/policies/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Policy Not Found"));
    }

    @Test
    void flagPolicies_shouldReturn200WithResult() throws Exception {
        List<UUID> ids = List.of(UUID.randomUUID());
        when(policyUseCase.flagPoliciesForReview(any()))
                .thenReturn(new PolicyUseCase.BulkFlagResult(1, ids, List.of()));

        BulkFlagRequest request = new BulkFlagRequest(ids);

        mockMvc.perform(patch("/api/v1/policies/flag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flaggedCount").value(1));
    }

    @Test
    void flagPolicies_emptyList_shouldReturn400() throws Exception {
        BulkFlagRequest request = new BulkFlagRequest(List.of());

        mockMvc.perform(patch("/api/v1/policies/flag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_shouldReturn200() throws Exception {
        when(policyUseCase.getSummary())
                .thenReturn(new PolicySummary(Map.of(), Map.of(), 5, 100, new BigDecimal("500000")));

        mockMvc.perform(get("/api/v1/policies/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActivePolicies").value(100))
                .andExpect(jsonPath("$.expiringSoonCount").value(5));
    }

    @Test
    void listPolicies_withStatusFilter_shouldReturn200() throws Exception {
        when(policyUseCase.listPolicies(any(), any()))
                .thenReturn(new PageImpl<>(List.of(samplePolicy())));

        mockMvc.perform(get("/api/v1/policies").param("status", "Active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("Active"));
    }

    @Test
    void listPolicies_invalidPageSize_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/policies").param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listPolicies_withSearch_shouldReturn200() throws Exception {
        when(policyUseCase.listPolicies(any(), any()))
                .thenReturn(new PageImpl<>(List.of(samplePolicy())));

        mockMvc.perform(get("/api/v1/policies").param("search", "Test Corp"))
                .andExpect(status().isOk());
    }
}
