package org.buratishkin.familyhub.family.poll.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record PollCreatedEvent(
        Long pollId,
        Long familyId,
        Long creatorMemberId,
        Long actorUserId,
        String question,
        String actorName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
