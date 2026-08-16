package org.buratishkin.familyhub.task.dto;

import jakarta.validation.constraints.NotNull;

public record TaskDeleteReq(
        @NotNull
        Long familyId
) {
}
