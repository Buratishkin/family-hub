package org.buratishkin.familyhub.family.invite.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record InviteCreatedEvent(
        Long inviteId,
        Long familyId,
        Long createdByMemberId,
        LocalDateTime expiresAt,
        LocalDateTime occurredAt
) implements DomainEvent {
}
