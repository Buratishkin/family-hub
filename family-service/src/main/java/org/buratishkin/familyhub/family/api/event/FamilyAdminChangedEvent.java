package org.buratishkin.familyhub.family.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record FamilyAdminChangedEvent(
        Long familyId,
        Long previousAdminMemberId,
        Long newAdminMemberId,
        LocalDateTime occurredAt
) implements DomainEvent {
}
