package org.buratishkin.familyhub.family.alias.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record AliasDeletedEvent(
        Long aliasId,
        Long familyId,
        Long ownerMemberId,
        Long targetMemberId,
        LocalDateTime occurredAt
) implements DomainEvent {
}
