package com.insurance.sample.infrastructure.messaging.kafka;

import com.insurance.sample.application.port.out.PolicyEventPublisher;
import com.insurance.sample.domain.event.PolicyFlaggedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaPolicyEventPublisher implements PolicyEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "policy.flagged";

    @Override
    public void publishFlagged(PolicyFlaggedEvent event) {
        kafkaTemplate.send(TOPIC, event.policyId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish PolicyFlaggedEvent for policyId={}: {}",
                                event.policyId(), ex.getMessage());
                    } else {
                        log.info("Published PolicyFlaggedEvent policyId={} to topic={} offset={}",
                                event.policyId(), TOPIC,
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
