package org.buratishkin.familyhub.task.api;

import java.time.LocalDateTime;

public record FamilyPlanView(
        Long id,
        Long familyId,
        Long memberId,
        String title,
        LocalDateTime busyFrom,
        LocalDateTime busyTo
) {
}
