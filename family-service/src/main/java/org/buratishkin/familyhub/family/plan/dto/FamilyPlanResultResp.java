package org.buratishkin.familyhub.family.plan.dto;

import java.time.LocalDateTime;

public record FamilyPlanResultResp(
        boolean result,
        String reason,
        FamilyPlanResp plan,
        LocalDateTime serverTime
) {
}
