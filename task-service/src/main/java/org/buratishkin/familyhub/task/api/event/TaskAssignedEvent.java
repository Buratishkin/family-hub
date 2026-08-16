package org.buratishkin.familyhub.task.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record TaskAssignedEvent(
        Long taskId,
        Long familyId,
        Long previousAssigneeMemberId,
        Long newAssigneeMemberId,
        String taskName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
