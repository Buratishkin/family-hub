package org.buratishkin.familyhub.task.api;

import java.time.LocalDateTime;

public record TaskView(
        Long id,
        Long familyId,
        Long creatorMemberId,
        Long assigneeMemberId,
        Long addressId,
        String name,
        String description,
        LocalDateTime start,
        String type,
        String status,
        Long recurrenceSeriesId,
        Long recurrenceRootTaskId,
        Long recurrenceParentTaskId,
        Integer recurrenceIndex,
        TaskRecurrenceView recurrence
) {
}
