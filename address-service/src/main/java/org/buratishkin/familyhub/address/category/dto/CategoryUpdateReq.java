package org.buratishkin.familyhub.address.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryUpdateReq(
        @NotNull
        Long familyId,
        @NotBlank
        String name
) {
}
