package com.insurance.sample.application.port.out;

import com.insurance.sample.domain.event.PolicyFlaggedEvent;

public interface PolicyEventPublisher {
    void publishFlagged(PolicyFlaggedEvent event);
}
