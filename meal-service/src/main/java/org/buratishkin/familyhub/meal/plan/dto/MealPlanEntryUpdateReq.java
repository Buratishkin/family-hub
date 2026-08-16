package org.buratishkin.familyhub.meal.plan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.buratishkin.familyhub.meal.plan.MealType;

import java.time.LocalDate;

public record MealPlanEntryUpdateReq(
        @NotNull
        Long familyId,
        LocalDate date,
        MealType mealType,
        Long recipeId,
        @Positive
        Integer servings,
        String note
) {
}
