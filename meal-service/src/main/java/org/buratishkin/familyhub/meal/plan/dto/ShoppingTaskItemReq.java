package org.buratishkin.familyhub.meal.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ShoppingTaskItemReq(
        @NotBlank
        String name,
        @NotNull
        @Positive
        BigDecimal amount,
        @NotBlank
        String unit
) {
}
