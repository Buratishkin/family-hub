package org.buratishkin.familyhub.meal.plan.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.meal.plan.dto.IngredientSummaryResp;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanDayResp;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryCreateReq;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryCreateResp;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryDeleteReq;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryUpdateReq;
import org.buratishkin.familyhub.meal.plan.dto.ShoppingTaskCreateReq;
import org.buratishkin.familyhub.meal.plan.dto.ShoppingTaskCreateResp;
import org.buratishkin.familyhub.meal.plan.service.MealPlanManagerService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meal-plan")
public class MealPlanController {
    private final MealPlanManagerService mealPlanManagerService;

    @PostMapping("/entries")
    public CreateResp<MealPlanEntryCreateResp> createEntry(@Valid @RequestBody MealPlanEntryCreateReq req,
                                                           Authentication authentication) {
        return mealPlanManagerService.createEntry(req, authentication);
    }

    @PutMapping("/entries/{entryId}")
    public UpdateResp updateEntry(@Valid @RequestBody MealPlanEntryUpdateReq req,
                                  @PathVariable Long entryId,
                                  Authentication authentication) {
        return mealPlanManagerService.updateEntry(req, entryId, authentication);
    }

    @DeleteMapping("/entries/{entryId}")
    public DeleteResp deleteEntry(@Valid @RequestBody MealPlanEntryDeleteReq req,
                                  @PathVariable Long entryId,
                                  Authentication authentication) {
        return mealPlanManagerService.deleteEntry(req, entryId, authentication);
    }

    @GetMapping("/families/{familyId}/days/{date}")
    public MealPlanDayResp findDay(@PathVariable Long familyId,
                                   @PathVariable LocalDate date,
                                   Authentication authentication) {
        return mealPlanManagerService.findDay(familyId, date, authentication);
    }

    @GetMapping("/families/{familyId}/days/{date}/ingredients")
    public List<IngredientSummaryResp> summarizeIngredients(@PathVariable Long familyId,
                                                            @PathVariable LocalDate date,
                                                            Authentication authentication) {
        return mealPlanManagerService.summarizeIngredients(familyId, date, authentication);
    }

    @PostMapping("/families/{familyId}/days/{date}/shopping-task")
    public CreateResp<ShoppingTaskCreateResp> createShoppingTask(@PathVariable Long familyId,
                                                                 @PathVariable LocalDate date,
                                                                 @Valid @RequestBody ShoppingTaskCreateReq req,
                                                                 Authentication authentication,
                                                                 HttpServletRequest request) {
        return mealPlanManagerService.createShoppingTask(
                familyId,
                date,
                req,
                authentication,
                request.getHeader("Authorization")
        );
    }
}
