package org.buratishkin.familyhub.meal.plan.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.meal.plan.MealPlanEntryEntity;
import org.buratishkin.familyhub.meal.plan.MealPlanEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MealPlanCrudService {
    private final MealPlanEntryRepository repository;

    public MealPlanEntryEntity save(MealPlanEntryEntity entry) {
        return repository.save(entry);
    }

    public MealPlanEntryEntity findById(Long entryId) {
        return repository.findById(entryId)
                .orElseThrow(() -> new IllegalStateException("Meal plan entry not found: " + entryId));
    }

    public Optional<MealPlanEntryEntity> findOptionalById(Long entryId) {
        return repository.findById(entryId);
    }

    public List<MealPlanEntryEntity> findDayEntries(Long familyId, LocalDate date) {
        return repository.findAllByFamilyIdAndPlannedDateOrderByMealTypeAscIdAsc(familyId, date);
    }

    public List<MealPlanEntryEntity> findRangeEntries(Long familyId, LocalDate from, LocalDate to) {
        return repository.findAllByFamilyIdAndPlannedDateBetweenOrderByPlannedDateAscMealTypeAscGroupIdAscIdAsc(familyId, from, to);
    }

    public List<MealPlanEntryEntity> findAllByIds(List<Long> ids) {
        return repository.findAllByIdIn(ids);
    }

    public void delete(MealPlanEntryEntity entry) {
        repository.delete(entry);
    }

    public void deleteAllByRecipeId(Long recipeId) {
        repository.deleteAllByRecipeId(recipeId);
    }
}
