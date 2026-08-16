package org.buratishkin.familyhub.meal.ingredient.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.meal.food.FoodCategory;
import org.buratishkin.familyhub.meal.ingredient.IngredientProductEntity;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BuiltInIngredientCatalogSeeder implements ApplicationRunner {
    private final IngredientProductCrudService ingredientProductCrudService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        try (InputStream inputStream = new ClassPathResource("food/default_food_ingredients_seed.json").getInputStream()) {
            SeedFile seedFile = objectMapper.readValue(inputStream, SeedFile.class);
            for (SeedIngredient ingredient : seedFile.items()) {
                String normalizedName = ingredientProductCrudService.normalize(ingredient.name());
                if (ingredientProductCrudService.builtInExists(normalizedName)) {
                    continue;
                }

                FoodCategory category = FoodCategory.byId(ingredient.categoryId());
                IngredientProductEntity product = new IngredientProductEntity();
                product.setId(ingredient.id());
                product.setFamilyId(null);
                product.setCreatedByUserId(null);
                product.setCreatedByMemberId(null);
                product.setName(ingredient.name());
                product.setCategory(category.displayName());
                product.setCategoryId(category.id());
                product.setBuiltIn(true);
                product.setCreatedAt(null);
                ingredientProductCrudService.save(product);
            }
        }
    }

    public record SeedFile(String idStrategy, String categoryFile, List<SeedIngredient> items) {
    }

    public record SeedIngredient(Long id, String name, Long categoryId, boolean isDefault) {
    }
}
