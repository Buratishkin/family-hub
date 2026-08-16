package org.buratishkin.familyhub.task.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record TaskCreatedEvent(
        Long taskId,
        Long familyId,
        Long creatorMemberId,
        Long assigneeMemberId,
        Long addressId,
        String taskName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
