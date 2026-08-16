package org.buratishkin.familyhub.family.alias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AliasCreateReq (
        @NotNull
        Long familyId,
        @NotNull
        Long targetId,
        @NotBlank
        String alias
) {
}
