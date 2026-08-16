package org.buratishkin.familyhub.task.api;

import java.util.List;

public interface TaskLookupApi {
    List<TaskView> findTasksByFamilyId(Long familyId);

    List<TaskView> findOldTasksByFamilyId(Long familyId);

    int countByAssigneeMemberId(Long memberId);

    int countByCreatorMemberId(Long memberId);
}
