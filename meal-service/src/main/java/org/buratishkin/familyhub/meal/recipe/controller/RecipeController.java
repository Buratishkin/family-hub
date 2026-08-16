package org.buratishkin.familyhub.meal.recipe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeCreateReq;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeCreateResp;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeDeleteReq;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeResp;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeUpdateReq;
import org.buratishkin.familyhub.meal.recipe.service.RecipeManagerService;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.shared.response.UpdateResp;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recipe")
public class RecipeController {
    private final RecipeManagerService recipeManagerService;

    @PostMapping("/")
    public CreateResp<RecipeCreateResp> create(@Valid @RequestBody RecipeCreateReq req,
                                               Authentication authentication) {
        return recipeManagerService.create(req, authentication);
    }

    @PutMapping("/{recipeId}")
    public UpdateResp update(@Valid @RequestBody RecipeUpdateReq req,
                             @PathVariable Long recipeId,
                             Authentication authentication) {
        return recipeManagerService.update(req, recipeId, authentication);
    }

    @DeleteMapping("/{recipeId}")
    public DeleteResp delete(@Valid @RequestBody RecipeDeleteReq req,
                             @PathVariable Long recipeId,
                             Authentication authentication) {
        return recipeManagerService.delete(req, recipeId, authentication);
    }

    @GetMapping("/{recipeId}")
    public RecipeResp findById(@PathVariable Long recipeId, Authentication authentication) {
        return recipeManagerService.findById(recipeId, authentication);
    }

    @GetMapping("/families/{familyId}")
    public List<RecipeResp> findAllByFamily(@PathVariable Long familyId, Authentication authentication) {
        return recipeManagerService.findAllByFamily(familyId, authentication);
    }
}
