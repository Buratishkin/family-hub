package org.buratishkin.familyhub.address.category.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record CategoryArchivedEvent(
        Long categoryId,
        Long familyId,
        Long archivedByUserId,
        String categoryName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
