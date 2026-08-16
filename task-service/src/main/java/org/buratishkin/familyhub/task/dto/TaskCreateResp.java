package org.buratishkin.familyhub.task.dto;

public record TaskCreateResp(
        Long taskId,
        Long recurrenceSeriesId
) {
}
