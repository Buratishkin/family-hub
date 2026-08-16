package org.buratishkin.familyhub.family.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record FamilyDeletedEvent(
        Long familyId,
        Long deletedByUserId,
        LocalDateTime occurredAt
) implements DomainEvent {
}
