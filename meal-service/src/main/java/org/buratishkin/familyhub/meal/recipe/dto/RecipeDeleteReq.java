package org.buratishkin.familyhub.meal.recipe.dto;

import jakarta.validation.constraints.NotNull;

public record RecipeDeleteReq(
        @NotNull
        Long familyId
) {
}
