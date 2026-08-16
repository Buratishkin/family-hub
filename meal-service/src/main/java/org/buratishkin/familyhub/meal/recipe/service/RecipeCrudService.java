package org.buratishkin.familyhub.meal.recipe.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.meal.recipe.RecipeEntity;
import org.buratishkin.familyhub.meal.recipe.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeCrudService {
    private final RecipeRepository recipeRepository;

    public RecipeEntity save(RecipeEntity recipe) {
        return recipeRepository.save(recipe);
    }

    public RecipeEntity findById(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalStateException("Recipe not found: " + recipeId));
    }

    public List<RecipeEntity> findAllByFamilyId(Long familyId) {
        return recipeRepository.findAllByFamilyIdOrderByNameAsc(familyId);
    }

    public List<RecipeEntity> findActiveByFamilyId(Long familyId) {
        return recipeRepository.findAllByFamilyIdAndArchivedFalseOrderByNameAsc(familyId);
    }

    public void delete(RecipeEntity recipe) {
        recipeRepository.delete(recipe);
    }
}
