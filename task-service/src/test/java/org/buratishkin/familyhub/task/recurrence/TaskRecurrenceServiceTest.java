package org.buratishkin.familyhub.task.recurrence;

import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.TaskRepository;
import org.buratishkin.familyhub.task.api.event.TaskDeletedEvent;
import org.buratishkin.familyhub.task.dto.TaskRecurrenceReq;
import org.buratishkin.familyhub.task.dto.TaskUpdateResultResp;
import org.buratishkin.familyhub.task.enums.TaskEnum;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import org.buratishkin.familyhub.task.reminder.TaskReminderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskRecurrenceServiceTest {
    @Mock
    private TaskRecurrenceSeriesRepository seriesRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private TaskReminderService reminderService;

    @InjectMocks
    private TaskRecurrenceService recurrenceService;

    @Test
    void createSeriesGeneratesFiveOpenOccurrencesAndFinishGeneratesNextOne() {
        LocalDateTime startsAt = LocalDateTime.of(2026, 8, 3, 9, 0);
        TaskEntity root = task(123L, startsAt);
        List<TaskEntity> savedTasks = new ArrayList<>();
        Map<Long, TaskRecurrenceSeriesEntity> seriesById = new HashMap<>();
        long[] nextTaskId = {124L};
        long[] nextSeriesId = {55L};

        when(seriesRepository.save(any(TaskRecurrenceSeriesEntity.class))).thenAnswer(invocation -> {
            TaskRecurrenceSeriesEntity series = invocation.getArgument(0);
            if (series.getId() == null) {
                series.setId(nextSeriesId[0]++);
            }
            seriesById.put(series.getId(), series);
            return series;
        });
        when(seriesRepository.findById(55L)).thenAnswer(invocation -> Optional.ofNullable(seriesById.get(55L)));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity task = invocation.getArgument(0);
            if (task.getId() == null) {
                task.setId(nextTaskId[0]++);
            }
            savedTasks.add(task);
            return task;
        });
        when(taskRepository.countByRecurrenceSeriesIdAndStatusIn(eq(55L), ArgumentMatchers.<Collection<TaskStatusEnum>>any()))
                .thenAnswer(invocation -> savedTasks.stream()
                        .filter(task -> Long.valueOf(55L).equals(task.getRecurrenceSeriesId()))
                        .filter(task -> task.getStatus() == TaskStatusEnum.SCHEDULED
                                || task.getStatus() == TaskStatusEnum.IN_PROGRESS)
                        .count());

        TaskRecurrenceSeriesEntity series = recurrenceService.createSeries(
                root,
                10L,
                7L,
                new TaskRecurrenceReq(TaskRecurrenceFrequency.WEEKLY, 1, null, 7)
        );

        assertThat(series.getId()).isEqualTo(55L);
        assertThat(series.getGeneratedOccurrencesCount()).isEqualTo(5);
        assertThat(root.getRecurrenceSeriesId()).isEqualTo(55L);
        assertThat(savedTasks)
                .filteredOn(task -> Long.valueOf(55L).equals(task.getRecurrenceSeriesId()))
                .hasSize(5)
                .extracting(TaskEntity::getRecurrenceIndex)
                .containsExactly(1, 2, 3, 4, 5);

        root.setStatus(TaskStatusEnum.FINISHED);
        TaskUpdateResultResp.NextTaskResp nextTask = recurrenceService.generateAfterFinish(root);

        assertThat(nextTask).isNotNull();
        assertThat(nextTask.id()).isEqualTo(128L);
        assertThat(nextTask.start()).isEqualTo(startsAt.plusWeeks(5));
        assertThat(series.getGeneratedOccurrencesCount()).isEqualTo(6);
    }

    @Test
    void deleteFutureTasksCancelsSeriesAndDeletesAnchorAndFollowingTasks() {
        TaskRecurrenceSeriesEntity series = new TaskRecurrenceSeriesEntity();
        series.setId(55L);
        series.setFamilyId(10L);
        series.setStatus(TaskRecurrenceSeriesStatus.ACTIVE);

        TaskEntity previous = recurringTask(201L, 55L, 1);
        TaskEntity anchor = recurringTask(202L, 55L, 2);
        TaskEntity futureOne = recurringTask(203L, 55L, 3);
        TaskEntity futureTwo = recurringTask(204L, 55L, 4);

        when(seriesRepository.save(series)).thenReturn(series);
        when(taskRepository.findAllByRecurrenceSeriesIdAndRecurrenceIndexGreaterThanEqualOrderByRecurrenceIndexAsc(55L, 2))
                .thenReturn(List.of(anchor, futureOne, futureTwo));

        List<Long> deletedTaskIds = recurrenceService.deleteFutureTasks(series, anchor, 7L, 99L);

        assertThat(series.getStatus()).isEqualTo(TaskRecurrenceSeriesStatus.CANCELLED);
        assertThat(series.getCancelledByMemberId()).isEqualTo(7L);
        assertThat(series.getCancelledAt()).isNotNull();
        assertThat(deletedTaskIds).containsExactly(202L, 203L, 204L);
        verify(taskRepository, never()).delete(previous);
        verify(reminderService).cancelPending(202L);
        verify(reminderService).cancelPending(203L);
        verify(reminderService).cancelPending(204L);
        verify(taskRepository).delete(anchor);
        verify(taskRepository).delete(futureOne);
        verify(taskRepository).delete(futureTwo);
        verify(domainEventPublisher, times(3)).publish(any(TaskDeletedEvent.class));
    }

    private static TaskEntity task(Long id, LocalDateTime start) {
        TaskEntity task = new TaskEntity();
        task.setId(id);
        task.setCreatorId(7L);
        task.setAssigneeId(8L);
        task.setName("Buy milk");
        task.setDescription("2 bottles");
        task.setStart(start);
        task.setType(TaskEnum.SHOPPING);
        task.setStatus(TaskStatusEnum.SCHEDULED);
        return task;
    }

    private static TaskEntity recurringTask(Long id, Long recurrenceSeriesId, Integer recurrenceIndex) {
        TaskEntity task = task(id, LocalDateTime.of(2026, 8, 3, 9, 0).plusDays(recurrenceIndex));
        task.setRecurrenceSeriesId(recurrenceSeriesId);
        task.setRecurrenceIndex(recurrenceIndex);
        return task;
    }
}
