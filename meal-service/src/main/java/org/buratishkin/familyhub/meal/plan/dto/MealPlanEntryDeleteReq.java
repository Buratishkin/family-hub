package org.buratishkin.familyhub.meal.plan.dto;

import jakarta.validation.constraints.NotNull;

public record MealPlanEntryDeleteReq(
        @NotNull
        Long familyId
) {
}
