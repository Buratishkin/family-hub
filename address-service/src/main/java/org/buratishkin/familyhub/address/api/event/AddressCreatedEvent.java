package org.buratishkin.familyhub.address.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record AddressCreatedEvent(
        Long addressId,
        Long familyId,
        Long categoryId,
        Long createdByUserId,
        String addressName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
