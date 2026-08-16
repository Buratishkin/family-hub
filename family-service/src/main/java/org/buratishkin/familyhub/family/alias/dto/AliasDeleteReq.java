package org.buratishkin.familyhub.family.alias.dto;

import jakarta.validation.constraints.NotNull;

public record AliasDeleteReq(
        @NotNull
        Long familyId
) {
}
