package org.buratishkin.familyhub.meal.recipe.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.meal.ingredient.IngredientProductEntity;
import org.buratishkin.familyhub.meal.ingredient.service.IngredientProductCrudService;
import org.buratishkin.familyhub.meal.recipe.RecipeEntity;
import org.buratishkin.familyhub.meal.recipe.RecipeIngredientEntity;
import org.buratishkin.familyhub.meal.recipe.dto.IngredientReq;
import org.buratishkin.familyhub.meal.recipe.dto.IngredientResp;
import org.buratishkin.familyhub.meal.recipe.dto.NutritionReq;
import org.buratishkin.familyhub.meal.recipe.dto.NutritionResp;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeCreateReq;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeResp;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeUpdateReq;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecipeMapper {
    private final IngredientProductCrudService ingredientProductCrudService;

    public RecipeEntity toEntity(RecipeCreateReq req, Long createdByUserId) {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setFamilyId(req.familyId());
        recipe.setCreatedByUserId(createdByUserId);
        recipe.setAuthorName(null);
        recipe.setName(req.name());
        recipe.setDescription(req.description());
        recipe.setServings(req.servings() == null ? 1 : req.servings());
        applyNutrition(req.nutrition(), recipe);
        recipe.setArchived(false);
        LocalDateTime now = LocalDateTime.now();
        recipe.setCreatedAt(now);
        recipe.setUpdatedAt(now);
        recipe.replaceIngredients(toIngredients(req.ingredients(), req.familyId()));
        return recipe;
    }

    public void applyUpdate(RecipeUpdateReq req, RecipeEntity recipe) {
        if (req.name() != null && !req.name().isBlank()) {
            recipe.setName(req.name());
        }
        if (req.description() != null) {
            recipe.setDescription(req.description());
        }
        if (req.servings() != null) {
            recipe.setServings(req.servings());
        }
        if (req.nutrition() != null) {
            applyNutrition(req.nutrition(), recipe);
        }
        if (req.ingredients() != null) {
            recipe.replaceIngredients(toIngredients(req.ingredients(), recipe.getFamilyId()));
        }
        recipe.setUpdatedAt(LocalDateTime.now());
    }

    public RecipeResp toResp(RecipeEntity recipe) {
        return new RecipeResp(
                recipe.getId(),
                recipe.getFamilyId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getServings(),
                new NutritionResp(recipe.getCalories(), recipe.getProtein(), recipe.getFat(), recipe.getCarbs()),
                recipe.getIngredients().stream()
                        .map(i -> new IngredientResp(i.getProductId(), i.getName(), i.getAmount(), i.getUnit()))
                        .toList()
        );
    }

    private void applyNutrition(NutritionReq nutrition, RecipeEntity recipe) {
        if (nutrition == null) {
            return;
        }
        recipe.setCalories(nutrition.calories());
        recipe.setProtein(nutrition.protein());
        recipe.setFat(nutrition.fat());
        recipe.setCarbs(nutrition.carbs());
    }

    private List<RecipeIngredientEntity> toIngredients(List<IngredientReq> ingredients, Long familyId) {
        if (ingredients == null) {
            return List.of();
        }
        return ingredients.stream()
                .map(ingredient -> toIngredient(ingredient, familyId))
                .toList();
    }

    private RecipeIngredientEntity toIngredient(IngredientReq req, Long familyId) {
        RecipeIngredientEntity ingredient = new RecipeIngredientEntity();
        ingredient.setProductId(req.productId());
        if (req.productId() != null) {
            IngredientProductEntity product = ingredientProductCrudService.findAvailableById(req.productId(), familyId);
            ingredient.setName(product.getName());
            ingredient.setCategoryId(product.getCategoryId());
            ingredient.setCategoryName(product.getCategory());
        } else {
            ingredient.setName(resolveIngredientName(req));
        }
        ingredient.setAmount(req.amount());
        ingredient.setUnit(req.unit());
        return ingredient;
    }

    private String resolveIngredientName(IngredientReq req) {
        if (req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("ingredient name is required");
        }
        return req.name().trim();
    }
}
