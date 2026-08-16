package org.buratishkin.familyhub.task.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.dto.TaskRecurrenceCancelReq;
import org.buratishkin.familyhub.task.dto.TaskRecurrenceCancelResp;
import org.buratishkin.familyhub.task.port.TaskFamilyAccessPort;
import org.buratishkin.familyhub.task.port.TaskMemberLookupPort;
import org.buratishkin.familyhub.task.recurrence.TaskRecurrenceSeriesEntity;
import org.buratishkin.familyhub.task.recurrence.TaskRecurrenceService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskRecurrenceSeriesManagerService {
    private final TaskFamilyAccessPort familyAccessPort;
    private final TaskMemberLookupPort memberLookupPort;
    private final TaskRecurrenceService recurrenceService;

    @Transactional
    public TaskRecurrenceCancelResp cancelRemaining(Long seriesId,
                                                    TaskRecurrenceCancelReq req,
                                                    Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        if (!familyAccessPort.hasFamilyAccess(user.id(), req.familyId())) {
            return falseResult("invalid familyId", seriesId, req.taskId());
        }

        TaskRecurrenceSeriesEntity series = recurrenceService.findById(seriesId);
        if (series == null || !Objects.equals(series.getFamilyId(), req.familyId())) {
            return falseResult("invalid recurrenceSeriesId", seriesId, req.taskId());
        }

        TaskEntity anchorTask = recurrenceService.findTaskById(req.taskId());
        if (!isTaskFromSeries(anchorTask, seriesId)) {
            return falseResult("invalid taskId", seriesId, req.taskId());
        }

        MemberView member = memberLookupPort.findMemberByFamilyIdAndUserId(req.familyId(), user.id());
        if (member == null) {
            return falseResult("invalid familyId", seriesId, req.taskId());
        }

        List<Long> deletedTaskIds = recurrenceService.deleteFutureTasks(series, anchorTask, member.id(), user.id());
        return new TaskRecurrenceCancelResp(
                true,
                "good data",
                deletedTaskIds,
                seriesId,
                req.taskId()
        );
    }

    private boolean isTaskFromSeries(TaskEntity task, Long seriesId) {
        return task != null
                && Objects.equals(task.getRecurrenceSeriesId(), seriesId)
                && task.getRecurrenceIndex() != null;
    }

    private TaskRecurrenceCancelResp falseResult(String reason, Long seriesId, Long taskId) {
        return new TaskRecurrenceCancelResp(
                false,
                reason,
                List.of(),
                seriesId,
                taskId
        );
    }
}
