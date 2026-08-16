package org.buratishkin.familyhub.task.port;

import org.buratishkin.familyhub.task.api.FamilyPlanView;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskFamilyPlanLookupPort {
    List<FamilyPlanView> findConflictingPlans(Long familyId, Long memberId, LocalDateTime start);
}
