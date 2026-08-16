package org.buratishkin.familyhub.meal.plan.dto;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

public record ShoppingTaskCreateReq(
        @Valid
        List<ShoppingTaskItemReq> items,
        Long assigneeId,
        Long addressId,
        LocalDateTime start
) {
}
