package org.buratishkin.familyhub.meal.plan.dto;

import java.math.BigDecimal;
import java.util.List;

public record IngredientSummaryResp(
        String name,
        BigDecimal amount,
        String unit,
        List<String> recipeNames
) {
}
