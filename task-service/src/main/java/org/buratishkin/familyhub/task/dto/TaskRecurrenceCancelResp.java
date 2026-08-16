package org.buratishkin.familyhub.task.dto;

import java.util.List;

public record TaskRecurrenceCancelResp(
        boolean result,
        String reason,
        List<Long> deletedTaskIds,
        Long recurrenceSeriesId,
        Long taskId
) {
}
