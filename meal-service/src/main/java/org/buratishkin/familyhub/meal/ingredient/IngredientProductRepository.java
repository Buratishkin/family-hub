package org.buratishkin.familyhub.meal.ingredient;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredientProductRepository extends JpaRepository<IngredientProductEntity, Long> {
    boolean existsByBuiltInTrueAndNormalizedName(String normalizedName);

    Optional<IngredientProductEntity> findByBuiltInTrueAndNormalizedName(String normalizedName);

    Optional<IngredientProductEntity> findByFamilyIdAndNormalizedName(Long familyId, String normalizedName);

    Optional<IngredientProductEntity> findByFamilyIdAndNormalizedNameAndCategoryId(Long familyId, String normalizedName, Long categoryId);

    Optional<IngredientProductEntity> findByBuiltInTrueAndNormalizedNameAndCategoryId(String normalizedName, Long categoryId);

    @Query("select product.id from IngredientProductEntity product order by product.id asc")
    List<Long> findAllIdsOrderByIdAsc();

    @Query("""
            select product from IngredientProductEntity product
            where (product.familyId is null or product.familyId = :familyId)
              and (:query is null or lower(product.name) like lower(concat('%', :query, '%')))
            order by product.builtIn desc, product.name asc
            """)
    List<IngredientProductEntity> findAvailable(
            @Param("familyId") Long familyId,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("""
            select product from IngredientProductEntity product
            where (product.familyId is null or product.familyId = :familyId)
            order by product.builtIn desc, product.name asc
            """)
    List<IngredientProductEntity> findAllAvailable(@Param("familyId") Long familyId);
}
