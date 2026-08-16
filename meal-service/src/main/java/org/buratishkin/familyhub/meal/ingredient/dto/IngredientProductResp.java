package org.buratishkin.familyhub.meal.ingredient.dto;

public record IngredientProductResp(
        Long id,
        Long familyId,
        String name,
        Long categoryId,
        String categoryName,
        boolean isDefault,
        Long createdByMemberId,
        String createdAt
) {
}
