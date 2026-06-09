package com.insurance.sample.infrastructure.messaging.kafka;

import com.insurance.sample.application.port.out.PolicyEventPublisher;
import com.insurance.sample.domain.event.PolicyFlaggedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpPolicyEventPublisher implements PolicyEventPublisher {

    @Override
    public void publishFlagged(PolicyFlaggedEvent event) {
        log.debug("[NoOp] PolicyFlaggedEvent skipped for policyId={} (Kafka disabled)", event.policyId());
    }
}
