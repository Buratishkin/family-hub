package org.buratishkin.familyhub.task.reminder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.api.event.TaskReminderDueEvent;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskReminderService {
    private static final List<Integer> REMINDER_MINUTES = List.of(120, 30, 15);
    private static final int DUE_BATCH_SIZE = 100;
    private static final List<TaskStatusEnum> REMINDER_STATUSES = List.of(
            TaskStatusEnum.SCHEDULED,
            TaskStatusEnum.IN_PROGRESS
    );

    private final TaskReminderRepository reminderRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public void scheduleForTask(Long familyId, TaskEntity task) {
        if (task == null || task.getId() == null) {
            return;
        }

        cancelPending(task.getId());
        if (!canSchedule(task)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Integer reminderMinutes : REMINDER_MINUTES) {
            LocalDateTime dueAt = task.getStart().minusMinutes(reminderMinutes);
            if (!dueAt.isAfter(now)) {
                continue;
            }
            TaskReminderEntity reminder = new TaskReminderEntity();
            reminder.setTaskId(task.getId());
            reminder.setFamilyId(familyId);
            reminder.setCreatorMemberId(task.getCreatorId());
            reminder.setAssigneeMemberId(task.getAssigneeId());
            reminder.setTaskName(task.getName());
            reminder.setTaskStart(task.getStart());
            reminder.setReminderMinutes(reminderMinutes);
            reminder.setDueAt(dueAt);
            reminder.setStatus(TaskReminderStatus.PENDING);
            reminder.setCreatedAt(now);
            reminderRepository.save(reminder);
        }
    }

    @Transactional
    public void cancelPending(Long taskId) {
        if (taskId == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (TaskReminderEntity reminder : reminderRepository.findAllByTaskIdAndStatus(taskId, TaskReminderStatus.PENDING)) {
            reminder.setStatus(TaskReminderStatus.CANCELED);
            reminder.setCanceledAt(now);
            reminderRepository.save(reminder);
        }
    }

    @Scheduled(fixedDelayString = "${family-hub.task.reminders.dispatch-delay-ms:60000}")
    @Transactional
    public void publishDueReminders() {
        List<TaskReminderEntity> reminders = reminderRepository
                .findByStatusAndDueAtLessThanEqualOrderByDueAtAscIdAsc(
                        TaskReminderStatus.PENDING,
                        LocalDateTime.now(),
                        PageRequest.of(0, DUE_BATCH_SIZE)
                );

        for (TaskReminderEntity reminder : reminders) {
            publishReminder(reminder);
        }
    }

    private void publishReminder(TaskReminderEntity reminder) {
        safePublish(new TaskReminderDueEvent(
                reminder.getId(),
                reminder.getTaskId(),
                reminder.getFamilyId(),
                reminder.getCreatorMemberId(),
                reminder.getAssigneeMemberId(),
                reminder.getTaskName(),
                reminder.getTaskStart(),
                reminder.getReminderMinutes(),
                LocalDateTime.now()
        ));
        reminder.setStatus(TaskReminderStatus.SENT);
        reminder.setSentAt(LocalDateTime.now());
        reminderRepository.save(reminder);
    }

    private boolean canSchedule(TaskEntity task) {
        return task.getStart() != null
                && task.getCreatorId() != null
                && task.getStatus() != null
                && REMINDER_STATUSES.contains(task.getStatus());
    }

    private void safePublish(DomainEvent event) {
        try {
            domainEventPublisher.publish(event);
        } catch (RuntimeException e) {
            log.warn("Failed to publish task reminder event {}", event.getClass().getName(), e);
            throw e;
        }
    }
}
