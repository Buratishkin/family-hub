package org.buratishkin.familyhub.meal.plan.dto;

import java.time.LocalDate;
import java.util.List;

public record MealPlanDayResp(
        Long familyId,
        LocalDate date,
        List<MealPlanEntryResp> entries
) {
}
