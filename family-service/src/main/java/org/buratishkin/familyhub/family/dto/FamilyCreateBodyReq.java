package org.buratishkin.familyhub.family.dto;

import jakarta.validation.constraints.NotBlank;

public record FamilyCreateBodyReq(
        @NotBlank
        String name,
        String description
) {
}
