package org.buratishkin.familyhub.task.dto;

import jakarta.validation.constraints.NotNull;
import org.buratishkin.familyhub.task.enums.TaskEnum;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;

public record TaskUpdateReq(
        @NotNull
        Long familyId,
        String name,
        Long assigneeId,
        Long addressId,
        @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
        LocalDateTime start,
        TaskEnum type,
        TaskStatusEnum status,
        String description
) {
}
