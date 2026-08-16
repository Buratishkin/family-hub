package org.buratishkin.familyhub.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressUpdateReq(
        @NotNull
        Long familyId,
        @NotNull
        Long categoryId,
        @NotBlank
        String name,
        String country,
        @NotBlank
        String city,
        @NotBlank
        String streetType,
        @NotBlank
        String street,
        String house,
        String apartment,
        String comment
) {
}
