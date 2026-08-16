package org.buratishkin.familyhub.family.plan.dto;

import java.time.LocalDateTime;
import java.util.List;

public record FamilyPlanListResp(
        boolean result,
        String reason,
        List<FamilyPlanResp> items,
        LocalDateTime serverTime
) {
}
