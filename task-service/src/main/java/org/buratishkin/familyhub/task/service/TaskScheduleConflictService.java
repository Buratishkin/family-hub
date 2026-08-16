package org.buratishkin.familyhub.task.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.TaskRepository;
import org.buratishkin.familyhub.task.api.FamilyPlanView;
import org.buratishkin.familyhub.task.dto.TaskScheduleConflictResp;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import org.buratishkin.familyhub.task.port.TaskFamilyPlanLookupPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskScheduleConflictService {
    private static final List<TaskStatusEnum> ACTIVE_STATUSES = List.of(
            TaskStatusEnum.SCHEDULED,
            TaskStatusEnum.IN_PROGRESS
    );

    private final TaskRepository taskRepository;
    private final TaskFamilyPlanLookupPort planLookupPort;

    public List<TaskScheduleConflictResp> findConflicts(Long familyId,
                                                        Long assigneeId,
                                                        LocalDateTime start,
                                                        Long excludedTaskId) {
        if (familyId == null || assigneeId == null || start == null) {
            return List.of();
        }

        List<TaskScheduleConflictResp> taskConflicts = taskRepository
                .findScheduleConflicts(assigneeId, start, ACTIVE_STATUSES, excludedTaskId)
                .stream()
                .map(task -> toTaskConflict(task, assigneeId))
                .toList();

        List<TaskScheduleConflictResp> planConflicts = planLookupPort.findConflictingPlans(familyId, assigneeId, start)
                .stream()
                .map(plan -> toPlanConflict(plan, assigneeId, start))
                .toList();

        return java.util.stream.Stream.concat(taskConflicts.stream(), planConflicts.stream()).toList();
    }

    public boolean isActive(TaskStatusEnum status) {
        return ACTIVE_STATUSES.contains(status);
    }

    private TaskScheduleConflictResp toTaskConflict(TaskEntity task, Long assigneeId) {
        return new TaskScheduleConflictResp(
                "TASK",
                assigneeId,
                task.getId(),
                null,
                task.getName(),
                task.getStart(),
                null,
                null
        );
    }

    private TaskScheduleConflictResp toPlanConflict(FamilyPlanView plan, Long assigneeId, LocalDateTime start) {
        return new TaskScheduleConflictResp(
                "PLAN",
                assigneeId,
                null,
                plan.id(),
                plan.title(),
                start,
                plan.busyFrom(),
                plan.busyTo()
        );
    }
}
