package org.buratishkin.familyhub.task.dto;

import java.time.LocalDateTime;

public record TaskScheduleConflictResp(
        String type,
        Long assigneeId,
        Long taskId,
        Long planId,
        String title,
        LocalDateTime start,
        LocalDateTime busyFrom,
        LocalDateTime busyTo
) {
}
