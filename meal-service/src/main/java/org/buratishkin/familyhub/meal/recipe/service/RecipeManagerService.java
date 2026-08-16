package org.buratishkin.familyhub.meal.recipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.meal.plan.service.MealPlanCrudService;
import org.buratishkin.familyhub.meal.port.MealFamilyAccessPort;
import org.buratishkin.familyhub.meal.recipe.RecipeEntity;
import org.buratishkin.familyhub.meal.recipe.api.event.RecipeCreatedEvent;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeCreateReq;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeCreateResp;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeDeleteReq;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeResp;
import org.buratishkin.familyhub.meal.recipe.dto.RecipeUpdateReq;
import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.shared.response.UpdateResp;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeManagerService {
    private final RecipeCrudService recipeCrudService;
    private final MealPlanCrudService mealPlanCrudService;
    private final RecipeMapper recipeMapper;
    private final MealFamilyAccessPort familyAccessPort;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public CreateResp<RecipeCreateResp> create(RecipeCreateReq req, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), req.familyId())) {
            return new CreateResp<>(false, "invalid familyId", null);
        }

        RecipeEntity recipe;
        try {
            recipe = recipeMapper.toEntity(req, user.id());
        } catch (IllegalArgumentException e) {
            return new CreateResp<>(false, "invalid ingredients", null);
        }
        recipeCrudService.save(recipe);
        safePublish(new RecipeCreatedEvent(
                recipe.getId(),
                recipe.getFamilyId(),
                user.id(),
                recipe.getName(),
                LocalDateTime.now()
        ));
        return new CreateResp<>(true, "good data", new RecipeCreateResp(recipe.getId()));
    }

    @Transactional
    public UpdateResp update(RecipeUpdateReq req, Long recipeId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), req.familyId())) {
            return new UpdateResp(false, "invalid familyId");
        }

        RecipeEntity recipe = recipeCrudService.findById(recipeId);
        if (!Objects.equals(recipe.getFamilyId(), req.familyId())) {
            return new UpdateResp(false, "invalid familyId");
        }

        try {
            recipeMapper.applyUpdate(req, recipe);
        } catch (IllegalArgumentException e) {
            return new UpdateResp(false, "invalid ingredients");
        }
        recipeCrudService.save(recipe);
        return new UpdateResp(true, "good data");
    }

    @Transactional
    public DeleteResp delete(RecipeDeleteReq req, Long recipeId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), req.familyId())) {
            return new DeleteResp(false, "invalid familyId");
        }

        RecipeEntity recipe = recipeCrudService.findById(recipeId);
        if (!Objects.equals(recipe.getFamilyId(), req.familyId())) {
            return new DeleteResp(false, "invalid familyId");
        }

        mealPlanCrudService.deleteAllByRecipeId(recipeId);
        recipeCrudService.delete(recipe);
        return new DeleteResp(true, "good data");
    }

    @Transactional(readOnly = true)
    public List<RecipeResp> findAllByFamily(Long familyId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), familyId)) {
            return List.of();
        }
        return recipeCrudService.findAllByFamilyId(familyId).stream()
                .map(recipeMapper::toResp)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeResp findById(Long recipeId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        RecipeEntity recipe = recipeCrudService.findById(recipeId);
        if (!familyAccessPort.hasFamilyAccess(user.id(), recipe.getFamilyId())) {
            throw new IllegalStateException("Recipe not found: " + recipeId);
        }
        return recipeMapper.toResp(recipe);
    }

    private void safePublish(DomainEvent event) {
        try {
            domainEventPublisher.publish(event);
        } catch (RuntimeException e) {
            log.warn("Failed to publish recipe domain event {}", event.getClass().getName(), e);
        }
    }
}
