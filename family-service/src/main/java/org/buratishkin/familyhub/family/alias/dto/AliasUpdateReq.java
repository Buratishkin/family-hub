package org.buratishkin.familyhub.family.alias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AliasUpdateReq(
        @NotNull
        Long familyId,
        @NotBlank
        String alias
) {
}
