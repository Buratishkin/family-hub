package org.buratishkin.familyhub.family.plan.dto;

import java.time.LocalDateTime;

public record FamilyPlanDeleteResp(
        boolean result,
        String reason,
        LocalDateTime serverTime
) {
}
