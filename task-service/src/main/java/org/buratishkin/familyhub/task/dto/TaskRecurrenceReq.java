package org.buratishkin.familyhub.task.dto;

import org.buratishkin.familyhub.task.recurrence.TaskRecurrenceFrequency;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;

public record TaskRecurrenceReq(
        TaskRecurrenceFrequency frequency,
        Integer interval,
        @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
        LocalDateTime repeatUntil,
        Integer maxOccurrences
) {
}
