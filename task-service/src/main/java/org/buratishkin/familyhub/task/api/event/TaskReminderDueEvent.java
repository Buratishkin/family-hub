package org.buratishkin.familyhub.task.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record TaskReminderDueEvent(
        Long taskReminderId,
        Long taskId,
        Long familyId,
        Long creatorMemberId,
        Long assigneeMemberId,
        String taskName,
        LocalDateTime taskStart,
        Integer reminderMinutes,
        LocalDateTime occurredAt
) implements DomainEvent {
}
