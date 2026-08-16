package org.buratishkin.familyhub.task.dto;

import org.buratishkin.familyhub.task.TaskEntity;

public record TaskChangeAssigneeResp(
        boolean result,
        String reason,
        TaskEntity task
) {
}
