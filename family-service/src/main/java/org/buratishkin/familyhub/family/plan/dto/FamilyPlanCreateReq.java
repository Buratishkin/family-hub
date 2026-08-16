package org.buratishkin.familyhub.family.plan.dto;

import java.time.LocalDateTime;

public record FamilyPlanCreateReq(
        String title,
        String description,
        LocalDateTime busyFrom,
        LocalDateTime busyTo
) {
}
