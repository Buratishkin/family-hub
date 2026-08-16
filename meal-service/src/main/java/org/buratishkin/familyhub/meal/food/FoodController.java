package org.buratishkin.familyhub.meal.food;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodDeleteReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodDeleteResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodIngredientCreateReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodIngredientCreateResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodIngredientsResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodMealPlanBatchReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodMealPlanBatchResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodMealPlanListResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeCreateResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeListResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodRecipeUpdateResp;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodShoppingTaskReq;
import org.buratishkin.familyhub.meal.food.FoodDtos.FoodShoppingTaskResp;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class FoodController {
    private final FoodManagerService foodManagerService;

    @GetMapping("/me/families/{familyId}/food/ingredients")
    public FoodIngredientsResp ingredients(@PathVariable Long familyId, Authentication authentication) {
        return foodManagerService.ingredients(familyId, authentication);
    }

    @PostMapping("/food/ingredient/")
    public FoodIngredientCreateResp createIngredient(@RequestBody FoodIngredientCreateReq req,
                                                     Authentication authentication) {
        return foodManagerService.createIngredient(req, authentication);
    }

    @GetMapping("/me/families/{familyId}/food/recipes")
    public FoodRecipeListResp recipes(@PathVariable Long familyId, Authentication authentication) {
        return foodManagerService.recipes(familyId, authentication);
    }

    @PostMapping("/food/recipe/")
    public FoodRecipeCreateResp createRecipe(@RequestBody FoodRecipeReq req, Authentication authentication) {
        return foodManagerService.createRecipe(req, authentication);
    }

    @PutMapping("/food/recipe/{recipeId}")
    public FoodRecipeUpdateResp updateRecipe(@PathVariable Long recipeId,
                                             @RequestBody FoodRecipeReq req,
                                             Authentication authentication) {
        return foodManagerService.updateRecipe(recipeId, req, authentication);
    }

    @DeleteMapping("/food/recipe/{recipeId}")
    public FoodDeleteResp deleteRecipe(@PathVariable Long recipeId,
                                       @RequestBody FoodDeleteReq req,
                                       Authentication authentication) {
        return foodManagerService.deleteRecipe(recipeId, req, authentication);
    }

    @GetMapping("/me/families/{familyId}/food/meal-plans")
    public FoodMealPlanListResp mealPlans(@PathVariable Long familyId,
                                          @RequestParam LocalDate from,
                                          @RequestParam LocalDate to,
                                          Authentication authentication) {
        return foodManagerService.mealPlans(familyId, from, to, authentication);
    }

    @PostMapping("/food/meal-plan/batch")
    public FoodMealPlanBatchResp createMealPlanBatch(@RequestBody FoodMealPlanBatchReq req,
                                                     Authentication authentication) {
        return foodManagerService.createMealPlanBatch(req, authentication);
    }

    @DeleteMapping("/me/families/{familyId}/food/meal-plans/{mealPlanId}")
    public FoodDeleteResp deleteMealPlanFromCalendar(@PathVariable Long familyId,
                                                     @PathVariable Long mealPlanId,
                                                     Authentication authentication) {
        return foodManagerService.deleteMealPlan(mealPlanId, familyId, authentication);
    }

    @DeleteMapping("/food/meal-plan/{mealPlanId}")
    public FoodDeleteResp deleteMealPlan(@PathVariable Long mealPlanId,
                                         @RequestBody(required = false) FoodDeleteReq req,
                                         Authentication authentication) {
        return foodManagerService.deleteMealPlan(mealPlanId, req, authentication);
    }

    @PostMapping("/food/shopping-task/")
    public FoodShoppingTaskResp createShoppingTask(@RequestBody FoodShoppingTaskReq req,
                                                   Authentication authentication,
                                                   HttpServletRequest request) {
        return foodManagerService.createShoppingTask(req, authentication, request.getHeader("Authorization"));
    }
}
