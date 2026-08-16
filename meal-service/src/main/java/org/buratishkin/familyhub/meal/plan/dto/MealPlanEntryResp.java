package org.buratishkin.familyhub.meal.plan.dto;

import org.buratishkin.familyhub.meal.plan.MealType;

import java.time.LocalDate;

public record MealPlanEntryResp(
        Long id,
        Long familyId,
        LocalDate date,
        MealType mealType,
        Long recipeId,
        String recipeName,
        Integer servings,
        String note
) {
}
