package org.buratishkin.familyhub.meal.ingredient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IngredientProductCreateReq(
        @NotNull
        Long familyId,
        @NotBlank
        String name,
        @NotNull
        Long categoryId
) {
}
