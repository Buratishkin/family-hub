package org.buratishkin.familyhub.shared.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record DomainEventEnvelope(
        int schemaVersion,
        UUID eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        LocalDateTime occurredAt,
        String payload
) {
}
