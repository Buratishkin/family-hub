package org.buratishkin.familyhub.task.dto;

import java.time.LocalDateTime;

public record TaskUpdateResultResp(
        boolean result,
        String reason,
        NextTaskResp nextTask,
        java.util.List<TaskScheduleConflictResp> conflicts
) {
    public record NextTaskResp(
            Long id,
            LocalDateTime start,
            Long recurrenceSeriesId
    ) {
    }
}
