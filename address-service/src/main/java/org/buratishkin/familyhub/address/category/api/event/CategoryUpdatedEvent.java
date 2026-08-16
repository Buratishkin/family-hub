package org.buratishkin.familyhub.address.category.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record CategoryUpdatedEvent(
        Long categoryId,
        Long familyId,
        Long updatedByUserId,
        String categoryName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
