package org.buratishkin.familyhub.meal.plan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "meal_plan_entry",
        indexes = {
                @Index(name = "idx_meal_plan_family_date", columnList = "family_id,planned_date"),
                @Index(name = "idx_meal_plan_recipe", columnList = "recipe_id")
        }
)
@Getter
@Setter
public class MealPlanEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType = MealType.SNACK;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(nullable = false)
    private Integer servings = 1;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String note;
}
