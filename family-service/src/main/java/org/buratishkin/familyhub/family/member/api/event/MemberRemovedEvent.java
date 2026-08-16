package org.buratishkin.familyhub.family.member.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record MemberRemovedEvent(
        Long familyId,
        Long memberId,
        Long userId,
        LocalDateTime occurredAt
) implements DomainEvent {
}
