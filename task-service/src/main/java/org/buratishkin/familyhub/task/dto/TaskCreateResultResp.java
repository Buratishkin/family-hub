package org.buratishkin.familyhub.task.dto;

import java.util.List;

public record TaskCreateResultResp(
        boolean result,
        String reason,
        TaskCreateResp createResp,
        List<TaskScheduleConflictResp> conflicts
) {
}
