package org.buratishkin.familyhub.task.reminder;

import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.api.event.TaskReminderDueEvent;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskReminderServiceTest {
    @Mock
    private TaskReminderRepository reminderRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private TaskReminderService reminderService;

    @Test
    void scheduleForTaskCreatesFutureReminderThresholdsOnly() {
        TaskEntity task = task(LocalDateTime.now().plusMinutes(40));
        when(reminderRepository.findAllByTaskIdAndStatus(1001L, TaskReminderStatus.PENDING)).thenReturn(List.of());

        reminderService.scheduleForTask(10L, task);

        ArgumentCaptor<TaskReminderEntity> captor = ArgumentCaptor.forClass(TaskReminderEntity.class);
        verify(reminderRepository).findAllByTaskIdAndStatus(1001L, TaskReminderStatus.PENDING);
        verify(reminderRepository, org.mockito.Mockito.times(2)).save(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(TaskReminderEntity::getReminderMinutes)
                .containsExactly(30, 15);
        assertThat(captor.getAllValues())
                .allSatisfy(reminder -> {
                    assertThat(reminder.getFamilyId()).isEqualTo(10L);
                    assertThat(reminder.getTaskId()).isEqualTo(1001L);
                    assertThat(reminder.getCreatorMemberId()).isEqualTo(77L);
                    assertThat(reminder.getAssigneeMemberId()).isEqualTo(88L);
                    assertThat(reminder.getTaskName()).isEqualTo("Buy milk");
                    assertThat(reminder.getStatus()).isEqualTo(TaskReminderStatus.PENDING);
                });
    }

    @Test
    void publishDueRemindersPublishesEventAndMarksSent() {
        TaskReminderEntity reminder = new TaskReminderEntity();
        reminder.setId(9001L);
        reminder.setTaskId(1001L);
        reminder.setFamilyId(10L);
        reminder.setCreatorMemberId(77L);
        reminder.setAssigneeMemberId(88L);
        reminder.setTaskName("Buy milk");
        reminder.setTaskStart(LocalDateTime.of(2026, 8, 12, 18, 0));
        reminder.setReminderMinutes(120);
        reminder.setDueAt(LocalDateTime.now().minusMinutes(1));
        reminder.setStatus(TaskReminderStatus.PENDING);
        reminder.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(reminderRepository.findByStatusAndDueAtLessThanEqualOrderByDueAtAscIdAsc(
                any(),
                any(),
                any()
        )).thenReturn(List.of(reminder));

        reminderService.publishDueReminders();

        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(TaskReminderDueEvent.class);
        TaskReminderDueEvent event = (TaskReminderDueEvent) eventCaptor.getValue();
        assertThat(event.taskReminderId()).isEqualTo(9001L);
        assertThat(event.taskId()).isEqualTo(1001L);
        assertThat(event.familyId()).isEqualTo(10L);
        assertThat(event.creatorMemberId()).isEqualTo(77L);
        assertThat(event.assigneeMemberId()).isEqualTo(88L);
        assertThat(event.reminderMinutes()).isEqualTo(120);
        assertThat(reminder.getStatus()).isEqualTo(TaskReminderStatus.SENT);
        assertThat(reminder.getSentAt()).isNotNull();
        verify(reminderRepository).save(reminder);
    }

    private TaskEntity task(LocalDateTime start) {
        TaskEntity task = new TaskEntity();
        task.setId(1001L);
        task.setCreatorId(77L);
        task.setAssigneeId(88L);
        task.setName("Buy milk");
        task.setStart(start);
        task.setStatus(TaskStatusEnum.SCHEDULED);
        return task;
    }
}
