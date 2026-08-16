package org.buratishkin.familyhub.address.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record AddressUpdatedEvent(
        Long addressId,
        Long familyId,
        Long categoryId,
        Long updatedByUserId,
        String addressName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
