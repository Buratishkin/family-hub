package org.buratishkin.familyhub.address.category.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record CategoryCreatedEvent(
        Long categoryId,
        Long familyId,
        Long createdByUserId,
        String categoryName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
