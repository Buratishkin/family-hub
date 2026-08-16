package org.buratishkin.familyhub.meal.recipe;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "recipe_entity",
        indexes = {
                @Index(name = "idx_recipe_family", columnList = "family_id"),
                @Index(name = "idx_recipe_family_name", columnList = "family_id,name")
        }
)
@Getter
@Setter
public class RecipeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "author_member_id")
    private Long authorMemberId;

    @Column(name = "author_name")
    private String authorName;

    @Column(nullable = false)
    private String name;

    private String category;

    private String description;

    @Column(nullable = false)
    private Integer servings = 1;

    private Integer calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbs;

    @Column(nullable = false)
    private boolean archived;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<RecipeIngredientEntity> ingredients = new ArrayList<>();

    public void replaceIngredients(List<RecipeIngredientEntity> newIngredients) {
        ingredients.clear();
        if (newIngredients == null) {
            return;
        }
        newIngredients.forEach(this::addIngredient);
    }

    public void addIngredient(RecipeIngredientEntity ingredient) {
        ingredient.setRecipe(this);
        ingredients.add(ingredient);
    }
}
