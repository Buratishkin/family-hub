package org.buratishkin.familyhub.meal.ingredient.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.meal.ingredient.dto.IngredientProductCreateReq;
import org.buratishkin.familyhub.meal.ingredient.dto.IngredientProductCreateResp;
import org.buratishkin.familyhub.meal.ingredient.dto.IngredientProductResp;
import org.buratishkin.familyhub.meal.ingredient.service.IngredientProductManagerService;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ingredient")
public class IngredientProductController {
    private final IngredientProductManagerService ingredientProductManagerService;

    @GetMapping("/families/{familyId}")
    public List<IngredientProductResp> search(@PathVariable Long familyId,
                                              @RequestParam(required = false) String query,
                                              @RequestParam(required = false) Integer limit,
                                              Authentication authentication) {
        return ingredientProductManagerService.search(familyId, query, limit, authentication);
    }

    @PostMapping("/")
    public CreateResp<IngredientProductCreateResp> create(@Valid @RequestBody IngredientProductCreateReq req,
                                                          Authentication authentication) {
        return ingredientProductManagerService.create(req, authentication);
    }
}
