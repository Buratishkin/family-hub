package org.buratishkin.familyhub.meal.plan.service;

import org.buratishkin.familyhub.meal.plan.MealPlanEntryEntity;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryCreateReq;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryResp;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryUpdateReq;
import org.buratishkin.familyhub.meal.recipe.RecipeEntity;
import org.springframework.stereotype.Component;

@Component
public class MealPlanMapper {
    public MealPlanEntryEntity toEntity(MealPlanEntryCreateReq req, Long createdByUserId) {
        MealPlanEntryEntity entry = new MealPlanEntryEntity();
        entry.setFamilyId(req.familyId());
        entry.setPlannedDate(req.date());
        entry.setMealType(req.mealType());
        entry.setRecipeId(req.recipeId());
        entry.setServings(req.servings() == null ? 1 : req.servings());
        entry.setNote(req.note());
        entry.setCreatedByUserId(createdByUserId);
        return entry;
    }

    public void applyUpdate(MealPlanEntryUpdateReq req, MealPlanEntryEntity entry) {
        if (req.date() != null) {
            entry.setPlannedDate(req.date());
        }
        if (req.mealType() != null) {
            entry.setMealType(req.mealType());
        }
        if (req.recipeId() != null) {
            entry.setRecipeId(req.recipeId());
        }
        if (req.servings() != null) {
            entry.setServings(req.servings());
        }
        if (req.note() != null) {
            entry.setNote(req.note());
        }
    }

    public MealPlanEntryResp toResp(MealPlanEntryEntity entry, RecipeEntity recipe) {
        return new MealPlanEntryResp(
                entry.getId(),
                entry.getFamilyId(),
                entry.getPlannedDate(),
                entry.getMealType(),
                entry.getRecipeId(),
                recipe.getName(),
                entry.getServings(),
                entry.getNote()
        );
    }
}
