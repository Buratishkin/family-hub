package org.buratishkin.familyhub.family.invite.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record InviteRedeemedEvent(
        Long inviteId,
        Long familyId,
        Long memberId,
        Long userId,
        LocalDateTime occurredAt
) implements DomainEvent {
}
