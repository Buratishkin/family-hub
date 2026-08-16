package org.buratishkin.familyhub.meal.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {
    List<RecipeEntity> findAllByFamilyIdOrderByNameAsc(Long familyId);

    List<RecipeEntity> findAllByFamilyIdAndArchivedFalseOrderByNameAsc(Long familyId);
}
