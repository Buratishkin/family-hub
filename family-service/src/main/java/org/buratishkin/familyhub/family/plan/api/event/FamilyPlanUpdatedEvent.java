package org.buratishkin.familyhub.family.plan.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record FamilyPlanUpdatedEvent(
        Long familyPlanId,
        Long familyId,
        Long memberId,
        Long actorUserId,
        String title,
        String actorName,
        LocalDateTime busyFrom,
        LocalDateTime busyTo,
        LocalDateTime occurredAt
) implements DomainEvent {
}
