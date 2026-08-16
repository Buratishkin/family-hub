package org.buratishkin.familyhub.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.buratishkin.familyhub.task.enums.TaskEnum;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;

public record TaskCreateReq(
        @NotNull
        Long familyId,
        @NotBlank
        String name,
        Long assigneeId,
        Long addressId,
        @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
        LocalDateTime start,
        TaskEnum type,
        String description,
        TaskRecurrenceReq recurrence
) {
}
