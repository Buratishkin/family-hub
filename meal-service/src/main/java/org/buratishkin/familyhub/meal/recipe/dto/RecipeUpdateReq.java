package org.buratishkin.familyhub.meal.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record RecipeUpdateReq(
        @NotNull
        Long familyId,
        String name,
        String description,
        @Positive
        Integer servings,
        @Valid
        NutritionReq nutrition,
        @Valid
        List<IngredientReq> ingredients
) {
}
