package org.buratishkin.familyhub.address.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record AddressDeletedEvent(
        Long addressId,
        Long familyId,
        Long deletedByUserId,
        String addressName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
