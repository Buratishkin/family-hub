package org.buratishkin.familyhub.meal.plan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.buratishkin.familyhub.meal.plan.MealType;

import java.time.LocalDate;

public record MealPlanEntryCreateReq(
        @NotNull
        Long familyId,
        @NotNull
        LocalDate date,
        @NotNull
        MealType mealType,
        @NotNull
        Long recipeId,
        @Positive
        Integer servings,
        String note
) {
}
