package org.buratishkin.familyhub.meal.food;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.buratishkin.familyhub.meal.plan.MealType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class FoodDtos {
    private FoodDtos() {
    }

    public record FoodCategoryResp(Long id, String code, String name) {
    }

    public record FoodIngredientResp(
            Long id,
            String name,
            Long categoryId,
            String categoryName,
            boolean isDefault,
            Long createdByMemberId,
            String createdAt
    ) {
    }

    public record FoodIngredientsResp(
            boolean result,
            String reason,
            List<FoodCategoryResp> categories,
            List<FoodIngredientResp> items,
            String serverTime
    ) {
    }

    public record FoodIngredientCreateReq(Long familyId, String name, Long categoryId) {
    }

    public record FoodIngredientCreateBody(Long ingredientId) {
    }

    public record FoodIngredientCreateResp(
            boolean result,
            String reason,
            FoodIngredientCreateBody createResp,
            FoodIngredientResp ingredient
    ) {
    }

    public record FoodRecipeIngredientReq(
            Long ingredientId,
            String name,
            Long categoryId,
            @JsonAlias("ampunt")
            String amount,
            String unit
    ) {
    }

    public record FoodRecipeIngredientResp(
            Long ingredientId,
            String name,
            Long categoryId,
            String categoryName,
            String amount,
            String unit
    ) {
    }

    public record FoodRecipeReq(
            Long familyId,
            String name,
            String category,
            String description,
            String calories,
            String proteins,
            String fats,
            String carbs,
            List<FoodRecipeIngredientReq> ingredients
    ) {
    }

    public record FoodRecipeResp(
            Long id,
            Long familyId,
            String name,
            String category,
            String description,
            Long authorMemberId,
            String authorName,
            String calories,
            String proteins,
            String fats,
            String carbs,
            List<FoodRecipeIngredientResp> ingredients,
            String createdAt,
            String updatedAt
    ) {
    }

    public record FoodRecipeListResp(boolean result, String reason, List<FoodRecipeResp> items, String serverTime) {
    }

    public record FoodRecipeCreateBody(Long recipeId) {
    }

    public record FoodRecipeCreateResp(
            boolean result,
            String reason,
            FoodRecipeCreateBody createResp,
            FoodRecipeResp recipe
    ) {
    }

    public record FoodRecipeUpdateResp(boolean result, String reason, FoodRecipeResp recipe) {
    }

    public record FoodDeleteReq(Long familyId) {
    }

    public record FoodDeleteResp(boolean result, String reason) {
    }

    public record FoodMealPlanResp(
            Long id,
            Long familyId,
            LocalDate date,
            MealType slot,
            Long groupId,
            Long recipeId,
            String recipeName,
            String note,
            String createdAt
    ) {
    }

    public record FoodMealPlanListResp(boolean result, String reason, List<FoodMealPlanResp> items, String serverTime) {
    }

    public record FoodMealPlanBatchReq(
            Long familyId,
            LocalDate date,
            MealType slot,
            String note,
            List<Long> recipeIds
    ) {
    }

    public record FoodMealPlanBatchBody(Long groupId, List<Long> mealPlanIds) {
    }

    public record FoodMealPlanBatchResp(
            boolean result,
            String reason,
            FoodMealPlanBatchBody createResp,
            List<FoodMealPlanResp> items
    ) {
    }

    public record FoodShoppingIngredientReq(
            Long recipeId,
            Long ingredientId,
            String name,
            Long categoryId,
            String amount,
            String unit,
            Boolean checked
    ) {
    }

    public record FoodShoppingTaskReq(
            Long familyId,
            LocalDate date,
            MealType slot,
            List<Long> mealPlanIds,
            Long assigneeId,
            List<FoodShoppingIngredientReq> selectedIngredients
    ) {
    }

    public record FoodShoppingTaskBody(Long taskId) {
    }

    public record FoodTaskResp(
            Long id,
            String name,
            String description,
            LocalDateTime start,
            String type,
            String status,
            Long creatorId,
            Long assigneeId,
            Long addressId
    ) {
    }

    public record FoodShoppingTaskResp(
            boolean result,
            String reason,
            FoodShoppingTaskBody createResp,
            FoodTaskResp task
    ) {
    }
}
