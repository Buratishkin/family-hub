package org.buratishkin.familyhub.family.plan.dto;

import java.time.LocalDateTime;

public record FamilyPlanResp(
        Long id,
        Long familyId,
        Long memberId,
        String memberName,
        String title,
        String description,
        LocalDateTime busyFrom,
        LocalDateTime busyTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
