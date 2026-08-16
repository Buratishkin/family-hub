package org.buratishkin.familyhub.family.plan.dto;

import java.time.LocalDateTime;

public record FamilyPlanCreateResultResp(
        boolean result,
        String reason,
        FamilyPlanCreateResp createResp,
        FamilyPlanResp plan,
        LocalDateTime serverTime
) {
}
