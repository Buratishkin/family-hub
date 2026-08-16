package org.buratishkin.familyhub.meal.food;

import java.util.Arrays;
import java.util.List;

public enum FoodCategory {
    MEAT_AND_POULTRY(1L, "MEAT_AND_POULTRY", "Мясо и птица"),
    FISH_AND_SEAFOOD(2L, "FISH_AND_SEAFOOD", "Рыба и морепродукты"),
    DAIRY_AND_EGGS(3L, "DAIRY_AND_EGGS", "Молочные продукты и яйца"),
    VEGETABLES(4L, "VEGETABLES", "Овощи"),
    FRUITS_AND_BERRIES(5L, "FRUITS_AND_BERRIES", "Фрукты и ягоды"),
    HERBS_AND_SALADS(6L, "HERBS_AND_SALADS", "Зелень и салаты"),
    GRAINS_LEGUMES_AND_PASTA(7L, "GRAINS_LEGUMES_AND_PASTA", "Крупы, бобовые и макароны"),
    BREAD_AND_BAKERY(8L, "BREAD_AND_BAKERY", "Хлеб и выпечка"),
    NUTS_SEEDS_AND_DRIED_FRUITS(9L, "NUTS_SEEDS_AND_DRIED_FRUITS", "Орехи, семена и сухофрукты"),
    OILS_SAUCES_AND_DRESSINGS(10L, "OILS_SAUCES_AND_DRESSINGS", "Масла, соусы и заправки"),
    SPICES_AND_SEASONINGS(11L, "SPICES_AND_SEASONINGS", "Специи и приправы"),
    CANNED_AND_PICKLED(12L, "CANNED_AND_PICKLED", "Консервы и заготовки"),
    SWEETS_AND_DESSERTS(13L, "SWEETS_AND_DESSERTS", "Сладкое и десерты"),
    DRINKS(14L, "DRINKS", "Напитки"),
    OTHER(15L, "OTHER", "Прочее");

    private final Long id;
    private final String code;
    private final String displayName;

    FoodCategory(Long id, String code, String displayName) {
        this.id = id;
        this.code = code;
        this.displayName = displayName;
    }

    public Long id() {
        return id;
    }

    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    public static FoodCategory byId(Long id) {
        return Arrays.stream(values())
                .filter(category -> category.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid categoryId"));
    }

    public static boolean exists(Long id) {
        return Arrays.stream(values()).anyMatch(category -> category.id.equals(id));
    }

    public static List<FoodCategory> all() {
        return List.of(values());
    }
}
