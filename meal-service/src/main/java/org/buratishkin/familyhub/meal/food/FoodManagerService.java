package org.buratishkin.familyhub.meal.food;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodCategoryResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodDeleteReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodDeleteResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodIngredientCreateBody;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodIngredientCreateReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodIngredientCreateResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodIngredientResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodIngredientsResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodMealPlanBatchBody;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodMealPlanBatchReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodMealPlanBatchResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodMealPlanListResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodMealPlanResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeCreateBody;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeCreateResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeIngredientReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeIngredientResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeListResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeUpdateResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodShoppingIngredientReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodShoppingTaskBody;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodShoppingTaskReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodShoppingTaskResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodTaskResp;
import org.buratishkin.familyhub.meal.ingredient.IngredientProductEntity;
import org.buratishkin.familyhub.meal.ingredient.service.IngredientProductCrudService;
import org.buratishkin.familyhub.meal.plan.MealPlanEntryEntity;
import org.buratishkin.familyhub.meal.plan.MealType;
import org.buratishkin.familyhub.meal.plan.client.TaskServiceClient;
import org.buratishkin.familyhub.meal.plan.service.MealPlanCrudService;
import org.buratishkin.familyhub.meal.port.MealFamilyAccessPort;
import org.buratishkin.familyhub.meal.projection.MealMemberProjectionEntity;
import org.buratishkin.familyhub.meal.projection.MealMemberProjectionRepository;
import org.buratishkin.familyhub.meal.recipe.RecipeEntity;
import org.buratishkin.familyhub.meal.recipe.RecipeIngredientEntity;
import org.buratishkin.familyhub.meal.recipe.api.event.RecipeCreatedEvent;
import org.buratishkin.familyhub.meal.recipe.service.RecipeCrudService;
import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodManagerService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final MealFamilyAccessPort familyAccessPort;
    private final MealMemberProjectionRepository memberProjectionRepository;
    private final IngredientProductCrudService ingredientProductCrudService;
    private final RecipeCrudService recipeCrudService;
    private final MealPlanCrudService mealPlanCrudService;
    private final TaskServiceClient taskServiceClient;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(readOnly = true)
    public FoodIngredientsResp ingredients(Long familyId, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return new FoodIngredientsResp(false, "invalid familyId", List.of(), List.of(), serverTime());
        }
        List<FoodCategoryResp> categories = FoodCategory.all().stream()
                .map(category -> new FoodCategoryResp(category.id(), category.code(), category.displayName()))
                .toList();
        List<FoodIngredientResp> items = ingredientProductCrudService.findAllAvailable(familyId).stream()
                .map(this::toIngredientResp)
                .toList();
        return new FoodIngredientsResp(true, "", categories, items, serverTime());
    }

    @Transactional
    public FoodIngredientCreateResp createIngredient(FoodIngredientCreateReq req, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), req.familyId())) {
            return new FoodIngredientCreateResp(false, "invalid familyId", null, null);
        }
        if (!FoodCategory.exists(req.categoryId())) {
            return new FoodIngredientCreateResp(false, "invalid categoryId", null, null);
        }
        String normalizedName = ingredientProductCrudService.normalize(req.name());
        if (ingredientProductCrudService.findExisting(req.familyId(), normalizedName, req.categoryId()).isPresent()) {
            return new FoodIngredientCreateResp(false, "ingredient already exists", null, null);
        }

        IngredientProductEntity ingredient = createCustomIngredient(req.familyId(), req.name(), req.categoryId(), user.id(), currentMemberId(req.familyId(), user.id()));
        return new FoodIngredientCreateResp(
                true,
                "",
                new FoodIngredientCreateBody(ingredient.getId()),
                toIngredientResp(ingredient)
        );
    }

    @Transactional(readOnly = true)
    public FoodRecipeListResp recipes(Long familyId, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return new FoodRecipeListResp(false, "invalid familyId", List.of(), serverTime());
        }
        return new FoodRecipeListResp(
                true,
                "",
                recipeCrudService.findActiveByFamilyId(familyId).stream().map(this::toRecipeResp).toList(),
                serverTime()
        );
    }

    @Transactional
    public FoodRecipeCreateResp createRecipe(FoodRecipeReq req, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), req.familyId())) {
            return new FoodRecipeCreateResp(false, "invalid familyId", null, null);
        }

        Long memberId = currentMemberId(req.familyId(), user.id());
        RecipeEntity recipe = new RecipeEntity();
        recipe.setFamilyId(req.familyId());
        recipe.setCreatedByUserId(user.id());
        recipe.setAuthorMemberId(memberId);
        recipe.setAuthorName(user.username());
        recipe.setName(req.name());
        recipe.setCategory(req.category());
        recipe.setDescription(req.description());
        recipe.setServings(1);
        try {
            applyNutrition(recipe, req);
        } catch (IllegalArgumentException e) {
            return new FoodRecipeCreateResp(false, e.getMessage(), null, null);
        }
        recipe.setArchived(false);
        LocalDateTime now = LocalDateTime.now();
        recipe.setCreatedAt(now);
        recipe.setUpdatedAt(now);

        try {
            recipe.replaceIngredients(toRecipeIngredients(req.familyId(), user.id(), memberId, req.ingredients()));
        } catch (IllegalArgumentException e) {
            return new FoodRecipeCreateResp(false, e.getMessage(), null, null);
        }

        recipeCrudService.save(recipe);
        safePublish(new RecipeCreatedEvent(
                recipe.getId(),
                recipe.getFamilyId(),
                user.id(),
                recipe.getName(),
                LocalDateTime.now()
        ));
        return new FoodRecipeCreateResp(true, "", new FoodRecipeCreateBody(recipe.getId()), toRecipeResp(recipe));
    }

    @Transactional
    public FoodRecipeUpdateResp updateRecipe(Long recipeId, FoodRecipeReq req, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), req.familyId())) {
            return new FoodRecipeUpdateResp(false, "invalid familyId", null);
        }
        RecipeEntity recipe = recipeCrudService.findById(recipeId);
        if (!Objects.equals(recipe.getFamilyId(), req.familyId()) || recipe.isArchived()) {
            return new FoodRecipeUpdateResp(false, "invalid recipeId", null);
        }
        if (req.name() != null && !req.name().isBlank()) {
            recipe.setName(req.name());
        }
        if (req.category() != null) {
            recipe.setCategory(req.category());
        }
        if (req.description() != null) {
            recipe.setDescription(req.description());
        }
        try {
            applyNutrition(recipe, req);
            if (req.ingredients() != null) {
                recipe.replaceIngredients(toRecipeIngredients(req.familyId(), user.id(), currentMemberId(req.familyId(), user.id()), req.ingredients()));
            }
        } catch (IllegalArgumentException e) {
            return new FoodRecipeUpdateResp(false, e.getMessage(), null);
        }
        recipe.setUpdatedAt(LocalDateTime.now());
        recipeCrudService.save(recipe);
        return new FoodRecipeUpdateResp(true, "", toRecipeResp(recipe));
    }

    @Transactional
    public FoodDeleteResp deleteRecipe(Long recipeId, FoodDeleteReq req, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), req.familyId())) {
            return new FoodDeleteResp(false, "invalid familyId");
        }
        RecipeEntity recipe = recipeCrudService.findById(recipeId);
        if (!Objects.equals(recipe.getFamilyId(), req.familyId())) {
            return new FoodDeleteResp(false, "invalid recipeId");
        }
        recipe.setArchived(true);
        recipe.setUpdatedAt(LocalDateTime.now());
        recipeCrudService.save(recipe);
        return new FoodDeleteResp(true, "");
    }

    @Transactional(readOnly = true)
    public FoodMealPlanListResp mealPlans(Long familyId, LocalDate from, LocalDate to, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return new FoodMealPlanListResp(false, "invalid familyId", List.of(), serverTime());
        }
        return new FoodMealPlanListResp(
                true,
                "",
                mealPlanCrudService.findRangeEntries(familyId, from, to).stream().map(this::toMealPlanResp).toList(),
                serverTime()
        );
    }

    @Transactional
    public FoodMealPlanBatchResp createMealPlanBatch(FoodMealPlanBatchReq req, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), req.familyId())) {
            return new FoodMealPlanBatchResp(false, "invalid familyId", null, List.of());
        }
        if (req.recipeIds() == null || req.recipeIds().isEmpty()) {
            return new FoodMealPlanBatchResp(false, "recipeIds are empty", null, List.of());
        }

        List<RecipeEntity> recipes = req.recipeIds().stream().map(recipeCrudService::findById).toList();
        if (recipes.stream().anyMatch(recipe -> !Objects.equals(recipe.getFamilyId(), req.familyId()) || recipe.isArchived())) {
            return new FoodMealPlanBatchResp(false, "invalid recipeId", null, List.of());
        }

        Long groupId = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        List<MealPlanEntryEntity> saved = new ArrayList<>();
        for (RecipeEntity recipe : recipes) {
            MealPlanEntryEntity entry = new MealPlanEntryEntity();
            entry.setFamilyId(req.familyId());
            entry.setPlannedDate(req.date());
            entry.setMealType(req.slot());
            entry.setGroupId(groupId);
            entry.setRecipeId(recipe.getId());
            entry.setServings(1);
            entry.setCreatedByUserId(user.id());
            entry.setCreatedAt(now);
            entry.setNote(req.note());
            saved.add(mealPlanCrudService.save(entry));
        }
        return new FoodMealPlanBatchResp(
                true,
                "",
                new FoodDtos.FoodMealPlanBatchBody(groupId, saved.stream().map(MealPlanEntryEntity::getId).toList()),
                saved.stream().map(this::toMealPlanResp).toList()
        );
    }

    @Transactional
    public FoodDeleteResp deleteMealPlan(Long mealPlanId, FoodDeleteReq req, Authentication authentication) {
        if (req == null || req.familyId() == null) {
            return new FoodDeleteResp(false, "invalid familyId");
        }
        return deleteMealPlan(mealPlanId, req.familyId(), authentication);
    }

    @Transactional
    public FoodDeleteResp deleteMealPlan(Long mealPlanId, Long familyId, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return new FoodDeleteResp(false, "invalid familyId");
        }
        MealPlanEntryEntity entry = mealPlanCrudService.findOptionalById(mealPlanId).orElse(null);
        if (entry == null) {
            return new FoodDeleteResp(false, "invalid mealPlanId");
        }
        if (!Objects.equals(entry.getFamilyId(), familyId)) {
            return new FoodDeleteResp(false, "invalid mealPlanId");
        }
        mealPlanCrudService.delete(entry);
        return new FoodDeleteResp(true, "");
    }

    @Transactional(readOnly = true)
    public FoodShoppingTaskResp createShoppingTask(FoodShoppingTaskReq req, Authentication authentication, String authorizationHeader) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), req.familyId())) {
            return new FoodShoppingTaskResp(false, "invalid familyId", null, null);
        }
        List<FoodShoppingIngredientReq> checkedIngredients = req.selectedIngredients() == null
                ? List.of()
                : req.selectedIngredients().stream().filter(item -> Boolean.TRUE.equals(item.checked())).toList();
        if (checkedIngredients.isEmpty()) {
            return new FoodShoppingTaskResp(false, "selected ingredients are empty", null, null);
        }

        List<MealPlanEntryEntity> mealPlans = mealPlanCrudService.findAllByIds(req.mealPlanIds() == null ? List.of() : req.mealPlanIds());
        if (mealPlans.size() != (req.mealPlanIds() == null ? 0 : req.mealPlanIds().size())
                || mealPlans.stream().anyMatch(plan -> !Objects.equals(plan.getFamilyId(), req.familyId()))) {
            return new FoodShoppingTaskResp(false, "meal plans do not belong to this family", null, null);
        }

        List<RecipeEntity> recipes = mealPlans.stream().map(plan -> recipeCrudService.findById(plan.getRecipeId())).toList();
        String taskName = shoppingTaskName(req.date(), req.slot());
        LocalDateTime start = LocalDateTime.of(req.date(), shoppingStartTime(req.slot()));
        String description = shoppingDescription(recipes, checkedIngredients);
        Long taskId = taskServiceClient.createShoppingTask(
                new TaskServiceClient.TaskCreateReq(req.familyId(), taskName, req.assigneeId(), null, start, "SHOPPING", description),
                authorizationHeader
        );
        Long creatorMemberId = currentMemberId(req.familyId(), user.id());
        FoodTaskResp task = new FoodTaskResp(taskId, taskName, description, start, "SHOPPING", "CREATED", creatorMemberId, req.assigneeId(), null);
        return new FoodShoppingTaskResp(true, "", new FoodShoppingTaskBody(taskId), task);
    }

    private UserView currentUser(Authentication authentication) {
        return familyAccessPort.currentUser(authentication.getName());
    }

    private boolean hasAccess(Long userId, Long familyId) {
        return familyAccessPort.hasFamilyAccess(userId, familyId);
    }

    private Long currentMemberId(Long familyId, Long userId) {
        return memberProjectionRepository.findByFamilyIdAndUserId(familyId, userId)
                .map(MealMemberProjectionEntity::getMemberId)
                .orElse(null);
    }

    private FoodIngredientResp toIngredientResp(IngredientProductEntity ingredient) {
        FoodCategory category = FoodCategory.byId(ingredient.getCategoryId());
        return new FoodIngredientResp(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getCategoryId(),
                category.displayName(),
                ingredient.isBuiltIn(),
                ingredient.getCreatedByMemberId(),
                format(ingredient.getCreatedAt())
        );
    }

    private IngredientProductEntity createCustomIngredient(Long familyId, String name, Long categoryId, Long userId, Long memberId) {
        IngredientProductEntity ingredient = new IngredientProductEntity();
        ingredient.setId(ingredientProductCrudService.nextCustomId());
        ingredient.setFamilyId(familyId);
        ingredient.setCreatedByUserId(userId);
        ingredient.setCreatedByMemberId(memberId);
        ingredient.setName(name.trim());
        FoodCategory category = FoodCategory.byId(categoryId);
        ingredient.setCategory(category.displayName());
        ingredient.setCategoryId(category.id());
        ingredient.setBuiltIn(false);
        ingredient.setCreatedAt(LocalDateTime.now());
        return ingredientProductCrudService.save(ingredient);
    }

    private void applyNutrition(RecipeEntity recipe, FoodRecipeReq req) {
        recipe.setCalories(parseInteger(req.calories()));
        recipe.setProtein(parseDecimal(req.proteins()));
        recipe.setFat(parseDecimal(req.fats()));
        recipe.setCarbs(parseDecimal(req.carbs()));
    }

    private List<RecipeIngredientEntity> toRecipeIngredients(Long familyId,
                                                            Long userId,
                                                            Long memberId,
                                                            List<FoodRecipeIngredientReq> ingredients) {
        if (ingredients == null) {
            return List.of();
        }
        List<RecipeIngredientEntity> result = new ArrayList<>();
        for (FoodRecipeIngredientReq req : ingredients) {
            IngredientProductEntity product = resolveIngredientProduct(familyId, userId, memberId, req);
            RecipeIngredientEntity recipeIngredient = new RecipeIngredientEntity();
            recipeIngredient.setProductId(product.getId());
            recipeIngredient.setName(product.getName());
            recipeIngredient.setCategoryId(product.getCategoryId());
            recipeIngredient.setCategoryName(FoodCategory.byId(product.getCategoryId()).displayName());
            recipeIngredient.setAmount(parseRequiredDecimal(req.amount(), "invalid amount"));
            recipeIngredient.setUnit(req.unit());
            result.add(recipeIngredient);
        }
        return result;
    }

    private IngredientProductEntity resolveIngredientProduct(Long familyId, Long userId, Long memberId, FoodRecipeIngredientReq req) {
        if (req.categoryId() == null || !FoodCategory.exists(req.categoryId())) {
            throw new IllegalArgumentException("invalid categoryId");
        }
        if (req.ingredientId() != null) {
            IngredientProductEntity product = ingredientProductCrudService.findAvailableById(req.ingredientId(), familyId);
            if (!Objects.equals(product.getCategoryId(), req.categoryId())) {
                throw new IllegalArgumentException("invalid categoryId");
            }
            return product;
        }
        if (req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("invalid ingredient");
        }
        String normalizedName = ingredientProductCrudService.normalize(req.name());
        return ingredientProductCrudService.findExisting(familyId, normalizedName, req.categoryId())
                .orElseGet(() -> createCustomIngredient(familyId, req.name(), req.categoryId(), userId, memberId));
    }

    private FoodRecipeResp toRecipeResp(RecipeEntity recipe) {
        return new FoodRecipeResp(
                recipe.getId(),
                recipe.getFamilyId(),
                recipe.getName(),
                recipe.getCategory(),
                recipe.getDescription(),
                recipe.getAuthorMemberId(),
                recipe.getAuthorName(),
                recipe.getCalories() == null ? null : recipe.getCalories().toString(),
                decimalToString(recipe.getProtein()),
                decimalToString(recipe.getFat()),
                decimalToString(recipe.getCarbs()),
                recipe.getIngredients().stream().map(this::toRecipeIngredientResp).toList(),
                format(recipe.getCreatedAt()),
                format(recipe.getUpdatedAt())
        );
    }

    private FoodRecipeIngredientResp toRecipeIngredientResp(RecipeIngredientEntity ingredient) {
        Long categoryId = ingredient.getCategoryId();
        String categoryName = categoryId == null ? null : FoodCategory.byId(categoryId).displayName();
        return new FoodRecipeIngredientResp(
                ingredient.getProductId(),
                ingredient.getName(),
                categoryId,
                categoryName,
                decimalToString(ingredient.getAmount()),
                ingredient.getUnit()
        );
    }

    private FoodMealPlanResp toMealPlanResp(MealPlanEntryEntity entry) {
        RecipeEntity recipe = recipeCrudService.findById(entry.getRecipeId());
        return new FoodMealPlanResp(
                entry.getId(),
                entry.getFamilyId(),
                entry.getPlannedDate(),
                entry.getMealType(),
                entry.getGroupId(),
                entry.getRecipeId(),
                recipe.getName(),
                entry.getNote(),
                format(entry.getCreatedAt())
        );
    }

    private String shoppingTaskName(LocalDate date, MealType slot) {
        return "Покупка на " + date.format(DateTimeFormatter.ofPattern("dd.MM")) + " " + slotDisplayName(slot);
    }

    private String shoppingDescription(List<RecipeEntity> recipes, List<FoodShoppingIngredientReq> ingredients) {
        Set<String> recipeNames = new LinkedHashSet<>();
        recipes.forEach(recipe -> recipeNames.add(recipe.getName()));

        StringBuilder builder = new StringBuilder("Блюда:");
        recipeNames.forEach(name -> builder.append(System.lineSeparator()).append("- ").append(name));
        builder.append(System.lineSeparator()).append(System.lineSeparator()).append("Купить:");
        ingredients.forEach(ingredient -> builder.append(System.lineSeparator())
                .append("- ")
                .append(ingredient.name())
                .append(" - ")
                .append(ingredient.amount())
                .append(" ")
                .append(ingredient.unit()));
        return builder.toString();
    }

    private LocalTime shoppingStartTime(MealType slot) {
        return switch (slot) {
            case BREAKFAST -> LocalTime.of(9, 0);
            case LUNCH -> LocalTime.of(13, 0);
            case DINNER -> LocalTime.of(19, 0);
            case SNACK -> LocalTime.of(16, 0);
        };
    }

    private String slotDisplayName(MealType slot) {
        return switch (slot) {
            case BREAKFAST -> "Завтрак";
            case LUNCH -> "Обед";
            case DINNER -> "Ужин";
            case SNACK -> "Перекус";
        };
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(normalizeNumber(value, "invalid nutrition"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid nutrition");
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseDecimalValue(value, "invalid nutrition");
    }

    private BigDecimal parseRequiredDecimal(String value, String reason) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(reason);
        }
        return parseDecimalValue(value, reason);
    }

    private BigDecimal parseDecimalValue(String value, String reason) {
        try {
            return new BigDecimal(normalizeNumber(value, reason));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(reason);
        }
    }

    private String normalizeNumber(String value, String reason) {
        String normalized = value
                .trim()
                .replace("\u00A0", "")
                .replace(" ", "")
                .replace(',', '.');
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(reason);
        }
        return normalized;
    }

    private String decimalToString(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }

    private String format(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }

    private String serverTime() {
        return format(LocalDateTime.now());
    }

    private void safePublish(DomainEvent event) {
        try {
            domainEventPublisher.publish(event);
        } catch (RuntimeException e) {
            log.warn("Failed to publish food domain event {}", event.getClass().getName(), e);
        }
    }
}
