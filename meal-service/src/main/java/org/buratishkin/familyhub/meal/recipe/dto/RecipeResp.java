package org.buratishkin.familyhub.meal.recipe.dto;

import java.util.List;

public record RecipeResp(
        Long id,
        Long familyId,
        String name,
        String description,
        Integer servings,
        NutritionResp nutrition,
        List<IngredientResp> ingredients
) {
}
