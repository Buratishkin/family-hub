package org.buratishkin.familyhub.task.dto;

import jakarta.validation.constraints.NotNull;

public record TaskRecurrenceCancelReq(
        @NotNull
        Long familyId,

        @NotNull
        Long taskId
) {
}
