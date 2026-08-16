package org.buratishkin.familyhub.meal.ingredient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ingredient_product",
        indexes = {
                @Index(name = "idx_ingredient_product_family", columnList = "family_id"),
                @Index(name = "idx_ingredient_product_normalized", columnList = "normalized_name")
        }
)
@Getter
@Setter
public class IngredientProductEntity {
    @Id
    private Long id;

    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_by_member_id")
    private Long createdByMemberId;

    @Column(nullable = false)
    private String name;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Column(nullable = false)
    private String category;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "built_in", nullable = false)
    private boolean builtIn;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
