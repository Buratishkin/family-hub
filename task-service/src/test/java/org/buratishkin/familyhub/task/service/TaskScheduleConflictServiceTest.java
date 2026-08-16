package org.buratishkin.familyhub.task.service;

import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.TaskRepository;
import org.buratishkin.familyhub.task.api.FamilyPlanView;
import org.buratishkin.familyhub.task.dto.TaskScheduleConflictResp;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import org.buratishkin.familyhub.task.port.TaskFamilyPlanLookupPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskScheduleConflictServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskFamilyPlanLookupPort planLookupPort;

    @InjectMocks
    private TaskScheduleConflictService conflictService;

    @Test
    void findConflictsReturnsTaskAndPlanConflictsForAssigneeAtStart() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 12, 14, 30);
        TaskEntity existingTask = task(1001L, 77L, start);
        FamilyPlanView plan = new FamilyPlanView(
                2001L,
                10L,
                77L,
                "Busy",
                LocalDateTime.of(2026, 8, 12, 14, 0),
                LocalDateTime.of(2026, 8, 12, 15, 0)
        );

        when(taskRepository.findScheduleConflicts(
                eq(77L),
                eq(start),
                ArgumentMatchers.<Collection<TaskStatusEnum>>any(),
                eq(999L)
        )).thenReturn(List.of(existingTask));
        when(planLookupPort.findConflictingPlans(10L, 77L, start)).thenReturn(List.of(plan));

        List<TaskScheduleConflictResp> conflicts = conflictService.findConflicts(10L, 77L, start, 999L);

        assertThat(conflicts)
                .extracting(TaskScheduleConflictResp::type)
                .containsExactly("TASK", "PLAN");
        assertThat(conflicts.getFirst().taskId()).isEqualTo(1001L);
        assertThat(conflicts.get(1).planId()).isEqualTo(2001L);
        assertThat(conflicts.get(1).busyFrom()).isEqualTo(LocalDateTime.of(2026, 8, 12, 14, 0));
        assertThat(conflicts.get(1).busyTo()).isEqualTo(LocalDateTime.of(2026, 8, 12, 15, 0));
    }

    @Test
    void findConflictsSkipsCheckWithoutAssigneeOrStart() {
        assertThat(conflictService.findConflicts(10L, null, LocalDateTime.now(), null)).isEmpty();
        assertThat(conflictService.findConflicts(10L, 77L, null, null)).isEmpty();
    }

    private static TaskEntity task(Long id, Long assigneeId, LocalDateTime start) {
        TaskEntity task = new TaskEntity();
        task.setId(id);
        task.setAssigneeId(assigneeId);
        task.setName("Existing task");
        task.setStart(start);
        return task;
    }
}
