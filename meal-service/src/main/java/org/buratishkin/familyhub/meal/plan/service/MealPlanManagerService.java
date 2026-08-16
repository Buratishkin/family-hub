package org.buratishkin.familyhub.meal.plan.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.meal.plan.MealPlanEntryEntity;
import org.buratishkin.familyhub.meal.plan.client.TaskServiceClient;
import org.buratishkin.familyhub.meal.plan.dto.IngredientSummaryResp;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanDayResp;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryCreateReq;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryCreateResp;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryDeleteReq;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryResp;
import org.buratishkin.familyhub.meal.plan.dto.MealPlanEntryUpdateReq;
import org.buratishkin.familyhub.meal.plan.dto.ShoppingTaskCreateReq;
import org.buratishkin.familyhub.meal.plan.dto.ShoppingTaskCreateResp;
import org.buratishkin.familyhub.meal.plan.dto.ShoppingTaskItemReq;
import org.buratishkin.familyhub.meal.port.MealFamilyAccessPort;
import org.buratishkin.familyhub.meal.recipe.RecipeEntity;
import org.buratishkin.familyhub.meal.recipe.RecipeIngredientEntity;
import org.buratishkin.familyhub.meal.recipe.service.RecipeCrudService;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.shared.response.UpdateResp;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MealPlanManagerService {
    private static final int AMOUNT_SCALE = 3;

    private final MealPlanCrudService mealPlanCrudService;
    private final RecipeCrudService recipeCrudService;
    private final MealPlanMapper mealPlanMapper;
    private final MealFamilyAccessPort familyAccessPort;
    private final TaskServiceClient taskServiceClient;

    @Transactional
    public CreateResp<MealPlanEntryCreateResp> createEntry(MealPlanEntryCreateReq req, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), req.familyId())) {
            return new CreateResp<>(false, "invalid familyId", null);
        }
        RecipeEntity recipe = recipeCrudService.findById(req.recipeId());
        if (!Objects.equals(recipe.getFamilyId(), req.familyId())) {
            return new CreateResp<>(false, "invalid recipeId", null);
        }

        MealPlanEntryEntity entry = mealPlanMapper.toEntity(req, user.id());
        mealPlanCrudService.save(entry);
        return new CreateResp<>(true, "good data", new MealPlanEntryCreateResp(entry.getId()));
    }

    @Transactional
    public UpdateResp updateEntry(MealPlanEntryUpdateReq req, Long entryId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), req.familyId())) {
            return new UpdateResp(false, "invalid familyId");
        }

        MealPlanEntryEntity entry = mealPlanCrudService.findById(entryId);
        if (!Objects.equals(entry.getFamilyId(), req.familyId())) {
            return new UpdateResp(false, "invalid familyId");
        }
        if (req.recipeId() != null) {
            RecipeEntity recipe = recipeCrudService.findById(req.recipeId());
            if (!Objects.equals(recipe.getFamilyId(), req.familyId())) {
                return new UpdateResp(false, "invalid recipeId");
            }
        }

        mealPlanMapper.applyUpdate(req, entry);
        mealPlanCrudService.save(entry);
        return new UpdateResp(true, "good data");
    }

    @Transactional
    public DeleteResp deleteEntry(MealPlanEntryDeleteReq req, Long entryId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), req.familyId())) {
            return new DeleteResp(false, "invalid familyId");
        }

        MealPlanEntryEntity entry = mealPlanCrudService.findById(entryId);
        if (!Objects.equals(entry.getFamilyId(), req.familyId())) {
            return new DeleteResp(false, "invalid familyId");
        }

        mealPlanCrudService.delete(entry);
        return new DeleteResp(true, "good data");
    }

    @Transactional(readOnly = true)
    public MealPlanDayResp findDay(Long familyId, LocalDate date, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), familyId)) {
            return new MealPlanDayResp(familyId, date, List.of());
        }

        List<MealPlanEntryResp> entries = mealPlanCrudService.findDayEntries(familyId, date).stream()
                .map(entry -> mealPlanMapper.toResp(entry, recipeCrudService.findById(entry.getRecipeId())))
                .toList();
        return new MealPlanDayResp(familyId, date, entries);
    }

    @Transactional(readOnly = true)
    public List<IngredientSummaryResp> summarizeIngredients(Long familyId, LocalDate date, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), familyId)) {
            return List.of();
        }

        Map<IngredientKey, MutableIngredientSummary> summaries = new LinkedHashMap<>();
        for (MealPlanEntryEntity entry : mealPlanCrudService.findDayEntries(familyId, date)) {
            RecipeEntity recipe = recipeCrudService.findById(entry.getRecipeId());
            if (!Objects.equals(recipe.getFamilyId(), familyId)) {
                continue;
            }
            BigDecimal factor = BigDecimal.valueOf(entry.getServings())
                    .divide(BigDecimal.valueOf(recipe.getServings()), AMOUNT_SCALE, RoundingMode.HALF_UP);
            for (RecipeIngredientEntity ingredient : recipe.getIngredients()) {
                IngredientKey key = IngredientKey.of(ingredient.getName(), ingredient.getUnit());
                MutableIngredientSummary summary = summaries.computeIfAbsent(
                        key,
                        ignored -> new MutableIngredientSummary(ingredient.getName(), ingredient.getUnit())
                );
                summary.add(ingredient.getAmount().multiply(factor), recipe.getName());
            }
        }

        return summaries.values().stream()
                .map(MutableIngredientSummary::toResp)
                .toList();
    }

    @Transactional(readOnly = true)
    public CreateResp<ShoppingTaskCreateResp> createShoppingTask(Long familyId,
                                                                LocalDate date,
                                                                ShoppingTaskCreateReq req,
                                                                Authentication authentication,
                                                                String authorizationHeader) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), familyId)) {
            return new CreateResp<>(false, "invalid familyId", null);
        }

        List<ShoppingTaskItemReq> selectedItems = req.items() == null
                ? List.of()
                : req.items();
        if (selectedItems.isEmpty()) {
            return new CreateResp<>(false, "empty shopping list", null);
        }

        String description = buildShoppingTaskDescription(date, selectedItems);
        Long taskId = taskServiceClient.createShoppingTask(
                new TaskServiceClient.TaskCreateReq(
                        familyId,
                        "Купить продукты на " + date,
                        req.assigneeId(),
                        req.addressId(),
                        req.start(),
                        "SHOPPING",
                        description
                ),
                authorizationHeader
        );
        return new CreateResp<>(true, "good data", new ShoppingTaskCreateResp(taskId));
    }

    private String buildShoppingTaskDescription(LocalDate date, List<ShoppingTaskItemReq> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("Продукты на ").append(date).append(":");
        for (ShoppingTaskItemReq item : items) {
            builder.append(System.lineSeparator())
                    .append("- ")
                    .append(item.name())
                    .append(" ")
                    .append(stripTrailingZeros(item.amount()))
                    .append(" ")
                    .append(item.unit());
        }
        return builder.toString();
    }

    private String stripTrailingZeros(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }

    private record IngredientKey(String normalizedName, String normalizedUnit) {
        static IngredientKey of(String name, String unit) {
            return new IngredientKey(
                    name.trim().toLowerCase(Locale.ROOT),
                    unit.trim().toLowerCase(Locale.ROOT)
            );
        }
    }

    private static class MutableIngredientSummary {
        private final String name;
        private final String unit;
        private final LinkedHashSet<String> recipeNames = new LinkedHashSet<>();
        private BigDecimal amount = BigDecimal.ZERO;

        private MutableIngredientSummary(String name, String unit) {
            this.name = name;
            this.unit = unit;
        }

        private void add(BigDecimal addedAmount, String recipeName) {
            amount = amount.add(addedAmount);
            recipeNames.add(recipeName);
        }

        private IngredientSummaryResp toResp() {
            return new IngredientSummaryResp(
                    name,
                    amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP).stripTrailingZeros(),
                    unit,
                    new ArrayList<>(recipeNames)
            );
        }
    }
}
