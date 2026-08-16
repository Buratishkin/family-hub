package org.buratishkin.familyhub.meal.plan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MealPlanEntryRepository extends JpaRepository<MealPlanEntryEntity, Long> {
    List<MealPlanEntryEntity> findAllByFamilyIdAndPlannedDateOrderByMealTypeAscIdAsc(Long familyId, LocalDate plannedDate);

    List<MealPlanEntryEntity> findAllByFamilyIdAndPlannedDateBetweenOrderByPlannedDateAscMealTypeAscGroupIdAscIdAsc(
            Long familyId,
            LocalDate from,
            LocalDate to
    );

    List<MealPlanEntryEntity> findAllByIdIn(List<Long> ids);

    void deleteAllByRecipeId(Long recipeId);
}
