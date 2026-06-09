package com.insurance.sample.infrastructure.messaging.kafka;

import com.insurance.sample.application.port.out.PolicyRepository;
import com.insurance.sample.domain.event.PolicyStatusChangedEvent;
import com.insurance.sample.domain.model.Policy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class PolicyStatusChangedConsumer {

    private final PolicyRepository policyRepository;
    // In-memory idempotency set — replace with Redis SET or DB table for production multi-node deployments
    private final Set<UUID> processedEvents = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "${app.kafka.topics.policy-status-changed:policy.status-changed}",
                   groupId = "${spring.kafka.consumer.group-id}")
    @CacheEvict(value = {"policy-detail", "policy-list", "policy-summary"}, allEntries = true)
    public void consume(@Payload PolicyStatusChangedEvent event, Acknowledgment ack) {
        if (processedEvents.contains(event.eventId())) {
            log.warn("Duplicate event ignored: eventId={}", event.eventId());
            ack.acknowledge();
            return;
        }

        log.info("Processing PolicyStatusChangedEvent eventId={} policyId={} {}->{}",
                event.eventId(), event.policyId(), event.previousStatus(), event.newStatus());

        Optional<Policy> policyOpt = policyRepository.findById(event.policyId());
        if (policyOpt.isEmpty()) {
            log.warn("Policy not found for status change event: policyId={}", event.policyId());
            ack.acknowledge();
            return;
        }

        Policy policy = policyOpt.get();
        policy.changeStatus(event.newStatus());
        policyRepository.saveAll(List.of(policy));

        processedEvents.add(event.eventId());
        ack.acknowledge();
        log.info("Status updated for policyId={} to {}", event.policyId(), event.newStatus());
    }
}
