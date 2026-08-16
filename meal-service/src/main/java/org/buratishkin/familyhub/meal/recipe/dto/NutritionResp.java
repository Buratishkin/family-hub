package org.buratishkin.familyhub.meal.recipe.dto;

import java.math.BigDecimal;

public record NutritionResp(
        Integer calories,
        BigDecimal protein,
        BigDecimal fat,
        BigDecimal carbs
) {
}
