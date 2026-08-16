package org.buratishkin.familyhub.meal.recipe.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record NutritionReq(
        @PositiveOrZero
        Integer calories,
        @PositiveOrZero
        BigDecimal protein,
        @PositiveOrZero
        BigDecimal fat,
        @PositiveOrZero
        BigDecimal carbs
) {
}
