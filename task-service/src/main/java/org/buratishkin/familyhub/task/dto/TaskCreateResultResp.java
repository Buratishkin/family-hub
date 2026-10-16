package org.buratishkin.familyhub.task.dto;

public record TaskCreateResultResp(
        boolean result,
        String reason,
        TaskCreateResp createResp
) {
}
