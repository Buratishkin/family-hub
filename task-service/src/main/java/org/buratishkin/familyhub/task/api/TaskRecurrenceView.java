package org.buratishkin.familyhub.task.api;

import java.time.LocalDateTime;

public record TaskRecurrenceView(
        String status,
        String frequency,
        Integer interval,
        LocalDateTime repeatUntil,
        Integer maxOccurrences
) {
}
