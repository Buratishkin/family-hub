package org.buratishkin.familyhub.meal.recipe.dto;

import java.math.BigDecimal;

public record IngredientResp(
        Long productId,
        String name,
        BigDecimal amount,
        String unit
) {
}
