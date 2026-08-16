package org.buratishkin.familyhub.shared.inbox;

import java.util.UUID;

public record KafkaDomainEventMessage(
        UUID eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        String payload
) {
}
