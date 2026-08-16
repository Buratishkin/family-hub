package org.buratishkin.familyhub.task.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record TaskDeletedEvent(
        Long taskId,
        Long familyId,
        Long deletedByUserId,
        String taskName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
