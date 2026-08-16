package org.buratishkin.familyhub.task.recurrence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.TaskRepository;
import org.buratishkin.familyhub.task.api.TaskRecurrenceView;
import org.buratishkin.familyhub.task.api.event.TaskAssignedEvent;
import org.buratishkin.familyhub.task.api.event.TaskCreatedEvent;
import org.buratishkin.familyhub.task.api.event.TaskDeletedEvent;
import org.buratishkin.familyhub.task.dto.TaskRecurrenceReq;
import org.buratishkin.familyhub.task.dto.TaskUpdateResultResp;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import org.buratishkin.familyhub.task.reminder.TaskReminderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskRecurrenceService {
    private static final int LOOKAHEAD_LIMIT = 5;
    private static final List<TaskStatusEnum> OPEN_STATUSES = List.of(
            TaskStatusEnum.SCHEDULED,
            TaskStatusEnum.IN_PROGRESS
    );

    private final TaskRecurrenceSeriesRepository seriesRepository;
    private final TaskRepository taskRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final TaskReminderService reminderService;

    public boolean isValid(TaskRecurrenceReq recurrence, LocalDateTime start) {
        if (recurrence == null) {
            return true;
        }
        if (start == null || recurrence.frequency() == null) {
            return false;
        }
        if (recurrence.interval() != null && recurrence.interval() < 1) {
            return false;
        }
        if (recurrence.maxOccurrences() != null && recurrence.maxOccurrences() < 1) {
            return false;
        }
        return recurrence.repeatUntil() == null || !recurrence.repeatUntil().isBefore(start);
    }

    public TaskRecurrenceSeriesEntity createSeries(TaskEntity rootTask,
                                                   Long familyId,
                                                   Long createdByMemberId,
                                                   TaskRecurrenceReq recurrence) {
        TaskRecurrenceSeriesEntity series = new TaskRecurrenceSeriesEntity();
        series.setFamilyId(familyId);
        series.setRootTaskId(rootTask.getId());
        series.setCreatedByMemberId(createdByMemberId);
        series.setStatus(TaskRecurrenceSeriesStatus.ACTIVE);
        series.setFrequency(recurrence.frequency());
        series.setIntervalValue(recurrence.interval() == null ? 1 : recurrence.interval());
        series.setStartsAt(rootTask.getStart());
        series.setRepeatUntil(recurrence.repeatUntil());
        series.setMaxOccurrences(recurrence.maxOccurrences());
        series.setGeneratedOccurrencesCount(1);
        series = seriesRepository.save(series);

        rootTask.setRecurrenceSeriesId(series.getId());
        rootTask.setRecurrenceRootTaskId(rootTask.getId());
        rootTask.setRecurrenceParentTaskId(null);
        rootTask.setRecurrenceIndex(1);
        taskRepository.save(rootTask);
        reminderService.scheduleForTask(familyId, rootTask);

        fillLookahead(series, rootTask);
        return series;
    }

    public TaskUpdateResultResp.NextTaskResp generateAfterFinish(TaskEntity finishedTask) {
        if (finishedTask.getRecurrenceSeriesId() == null
                || finishedTask.getStatus() != TaskStatusEnum.FINISHED) {
            return null;
        }
        TaskRecurrenceSeriesEntity series = seriesRepository.findById(finishedTask.getRecurrenceSeriesId())
                .orElse(null);
        if (!isActive(series)) {
            return null;
        }
        if (countOpenTasks(series.getId()) >= LOOKAHEAD_LIMIT) {
            return null;
        }
        TaskEntity nextTask = generateNext(series, finishedTask);
        if (nextTask == null) {
            return null;
        }
        return new TaskUpdateResultResp.NextTaskResp(
                nextTask.getId(),
                nextTask.getStart(),
                nextTask.getRecurrenceSeriesId()
        );
    }

    public TaskRecurrenceView toView(Long recurrenceSeriesId) {
        if (recurrenceSeriesId == null) {
            return null;
        }
        return seriesRepository.findById(recurrenceSeriesId)
                .map(series -> new TaskRecurrenceView(
                        series.getStatus() == null ? null : series.getStatus().name(),
                        series.getFrequency() == null ? null : series.getFrequency().name(),
                        series.getIntervalValue(),
                        series.getRepeatUntil(),
                        series.getMaxOccurrences()
                ))
                .orElse(null);
    }

    public TaskRecurrenceSeriesEntity findById(Long seriesId) {
        return seriesRepository.findById(seriesId).orElse(null);
    }

    public TaskEntity findTaskById(Long taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }

    public List<Long> deleteFutureTasks(TaskRecurrenceSeriesEntity series,
                                        TaskEntity anchorTask,
                                        Long cancelledByMemberId,
                                        Long deletedByUserId) {
        series.setStatus(TaskRecurrenceSeriesStatus.CANCELLED);
        series.setCancelledAt(LocalDateTime.now());
        series.setCancelledByMemberId(cancelledByMemberId);
        seriesRepository.save(series);

        List<TaskEntity> tasks = taskRepository
                .findAllByRecurrenceSeriesIdAndRecurrenceIndexGreaterThanEqualOrderByRecurrenceIndexAsc(
                        series.getId(),
                        anchorTask.getRecurrenceIndex()
                );
        for (TaskEntity task : tasks) {
            Long taskId = task.getId();
            String taskName = task.getName();
            reminderService.cancelPending(taskId);
            taskRepository.delete(task);
            safePublish(new TaskDeletedEvent(
                    taskId,
                    series.getFamilyId(),
                    deletedByUserId,
                    taskName,
                    LocalDateTime.now()
            ));
        }
        return tasks.stream()
                .map(TaskEntity::getId)
                .toList();
    }

    private void fillLookahead(TaskRecurrenceSeriesEntity series, TaskEntity rootTask) {
        TaskEntity parent = rootTask;
        while (countOpenTasks(series.getId()) < LOOKAHEAD_LIMIT) {
            TaskEntity generated = generateNext(series, parent);
            if (generated == null) {
                return;
            }
            parent = generated;
        }
    }

    private TaskEntity generateNext(TaskRecurrenceSeriesEntity series, TaskEntity parentTask) {
        if (!isActive(series)) {
            return null;
        }
        int nextIndex = series.getGeneratedOccurrencesCount() + 1;
        if (series.getMaxOccurrences() != null && nextIndex > series.getMaxOccurrences()) {
            return null;
        }
        LocalDateTime nextStart = calculateStart(series, nextIndex);
        if (series.getRepeatUntil() != null && nextStart.isAfter(series.getRepeatUntil())) {
            return null;
        }

        TaskEntity task = copyOccurrence(parentTask);
        task.setStart(nextStart);
        task.setStatus(TaskStatusEnum.SCHEDULED);
        task.setRecurrenceSeriesId(series.getId());
        task.setRecurrenceRootTaskId(series.getRootTaskId());
        task.setRecurrenceParentTaskId(parentTask.getId());
        task.setRecurrenceIndex(nextIndex);
        task = taskRepository.save(task);

        series.setGeneratedOccurrencesCount(nextIndex);
        seriesRepository.save(series);
        reminderService.scheduleForTask(series.getFamilyId(), task);
        publishCreated(series.getFamilyId(), task);
        return task;
    }

    private TaskEntity copyOccurrence(TaskEntity source) {
        TaskEntity task = new TaskEntity();
        task.setCreatorId(source.getCreatorId());
        task.setAssigneeId(source.getAssigneeId());
        task.setAddressId(source.getAddressId());
        task.setName(source.getName());
        task.setDescription(source.getDescription());
        task.setType(source.getType());
        return task;
    }

    private LocalDateTime calculateStart(TaskRecurrenceSeriesEntity series, int occurrenceIndex) {
        long steps = (long) (occurrenceIndex - 1) * series.getIntervalValue();
        return switch (series.getFrequency()) {
            case DAILY -> series.getStartsAt().plusDays(steps);
            case WEEKLY -> series.getStartsAt().plusWeeks(steps);
            case MONTHLY -> series.getStartsAt().plusMonths(steps);
        };
    }

    private long countOpenTasks(Long seriesId) {
        return taskRepository.countByRecurrenceSeriesIdAndStatusIn(seriesId, OPEN_STATUSES);
    }

    private boolean isActive(TaskRecurrenceSeriesEntity series) {
        return series != null && series.getStatus() == TaskRecurrenceSeriesStatus.ACTIVE;
    }

    private void publishCreated(Long familyId, TaskEntity task) {
        safePublish(new TaskCreatedEvent(
                task.getId(),
                familyId,
                task.getCreatorId(),
                task.getAssigneeId(),
                task.getAddressId(),
                task.getName(),
                LocalDateTime.now()
        ));
        if (task.getAssigneeId() != null) {
            safePublish(new TaskAssignedEvent(
                    task.getId(),
                    familyId,
                    null,
                    task.getAssigneeId(),
                    task.getName(),
                    LocalDateTime.now()
            ));
        }
    }

    private void safePublish(DomainEvent event) {
        try {
            domainEventPublisher.publish(event);
        } catch (RuntimeException e) {
            log.warn("Failed to publish task recurrence domain event {}", event.getClass().getName(), e);
        }
    }
}
