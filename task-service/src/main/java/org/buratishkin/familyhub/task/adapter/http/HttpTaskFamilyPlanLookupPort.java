package org.buratishkin.familyhub.task.adapter.http;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.task.api.FamilyPlanView;
import org.buratishkin.familyhub.task.port.TaskFamilyPlanLookupPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HttpTaskFamilyPlanLookupPort implements TaskFamilyPlanLookupPort {
    private final FamilyServiceInternalClient familyServiceInternalClient;

    @Override
    public List<FamilyPlanView> findConflictingPlans(Long familyId, Long memberId, LocalDateTime start) {
        return familyServiceInternalClient.findConflictingPlans(familyId, memberId, start);
    }
}
