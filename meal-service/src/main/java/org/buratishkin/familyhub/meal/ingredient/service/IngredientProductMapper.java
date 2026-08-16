package org.buratishkin.familyhub.meal.ingredient.service;

import org.buratishkin.familyhub.meal.ingredient.IngredientProductEntity;
import org.buratishkin.familyhub.meal.ingredient.dto.IngredientProductResp;
import org.buratishkin.familyhub.meal.food.FoodCategory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class IngredientProductMapper {
    public IngredientProductResp toResp(IngredientProductEntity product) {
        FoodCategory category = FoodCategory.byId(product.getCategoryId());
        return new IngredientProductResp(
                product.getId(),
                product.getFamilyId(),
                product.getName(),
                product.getCategoryId(),
                category.displayName(),
                product.isBuiltIn(),
                product.getCreatedByMemberId(),
                product.getCreatedAt() == null ? null : product.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
