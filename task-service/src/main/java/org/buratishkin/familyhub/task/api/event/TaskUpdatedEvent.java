package org.buratishkin.familyhub.task.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record TaskUpdatedEvent(
        Long taskId,
        Long familyId,
        Long updatedByUserId,
        String taskName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
