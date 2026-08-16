package org.buratishkin.familyhub.family.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record FamilyCreatedEvent(
        Long familyId,
        Long adminMemberId,
        Long adminUserId,
        LocalDateTime occurredAt
) implements DomainEvent {
}
