package org.buratishkin.familyhub.task.service;

import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.family.member.enums.MemberRole;
import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.TaskRepository;
import org.buratishkin.familyhub.task.api.TaskView;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import org.buratishkin.familyhub.task.port.TaskMemberLookupPort;
import org.buratishkin.familyhub.task.recurrence.TaskRecurrenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskCrudServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMemberLookupPort memberLookupPort;

    @Mock
    private TaskRecurrenceService recurrenceService;

    @InjectMocks
    private TaskCrudService taskCrudService;

    @Test
    void findTasksByFamilyIdReturnsCurrentTasks() {
        Long familyId = 10L;
        MemberView creator = member(1L, familyId);
        when(memberLookupPort.findMembersByFamilyId(familyId)).thenReturn(List.of(creator, member(2L, familyId)));
        when(memberLookupPort.findMemberById(creator.id())).thenReturn(creator);
        when(taskRepository.findCurrentByCreatorIdIn(
                eq(List.of(1L, 2L)),
                eq(List.of(TaskStatusEnum.SCHEDULED, TaskStatusEnum.IN_PROGRESS)),
                any(LocalDateTime.class)
        ))
                .thenReturn(List.of(task(100L, creator.id(), LocalDateTime.now().plusHours(1))));

        List<TaskView> tasks = taskCrudService.findTasksByFamilyId(familyId);

        assertThat(tasks)
                .singleElement()
                .extracting(TaskView::id, TaskView::familyId, TaskView::creatorMemberId)
                .containsExactly(100L, familyId, creator.id());
        verify(taskRepository).findCurrentByCreatorIdIn(
                eq(List.of(1L, 2L)),
                eq(List.of(TaskStatusEnum.SCHEDULED, TaskStatusEnum.IN_PROGRESS)),
                any(LocalDateTime.class)
        );
    }

    @Test
    void findOldTasksByFamilyIdReturnsOldTasks() {
        Long familyId = 10L;
        MemberView creator = member(1L, familyId);
        when(memberLookupPort.findMembersByFamilyId(familyId)).thenReturn(List.of(creator));
        when(memberLookupPort.findMemberById(creator.id())).thenReturn(creator);
        when(taskRepository.findOldByCreatorIdIn(
                eq(List.of(1L)),
                eq(List.of(TaskStatusEnum.FINISHED, TaskStatusEnum.CANCELED)),
                any(LocalDateTime.class)
        ))
                .thenReturn(List.of(task(99L, creator.id(), LocalDateTime.now().minusHours(1))));

        List<TaskView> tasks = taskCrudService.findOldTasksByFamilyId(familyId);

        assertThat(tasks)
                .singleElement()
                .extracting(TaskView::id, TaskView::familyId, TaskView::creatorMemberId)
                .containsExactly(99L, familyId, creator.id());
        verify(taskRepository).findOldByCreatorIdIn(
                eq(List.of(1L)),
                eq(List.of(TaskStatusEnum.FINISHED, TaskStatusEnum.CANCELED)),
                any(LocalDateTime.class)
        );
    }

    @Test
    void findTasksByFamilyIdDoesNotQueryTasksWhenFamilyHasNoMembers() {
        Long familyId = 10L;
        when(memberLookupPort.findMembersByFamilyId(familyId)).thenReturn(List.of());

        List<TaskView> tasks = taskCrudService.findTasksByFamilyId(familyId);

        assertThat(tasks).isEmpty();
        verify(taskRepository, never()).findCurrentByCreatorIdIn(anyCollection(), anyCollection(), any(LocalDateTime.class));
        verify(taskRepository, never()).findOldByCreatorIdIn(anyCollection(), anyCollection(), any(LocalDateTime.class));
    }

    private static MemberView member(Long id, Long familyId) {
        return new MemberView(id, familyId, 100L + id, "user" + id, MemberRole.MEMBER, "User " + id, null);
    }

    private static TaskEntity task(Long id, Long creatorId, LocalDateTime start) {
        TaskEntity task = new TaskEntity();
        task.setId(id);
        task.setCreatorId(creatorId);
        task.setName("Task " + id);
        task.setStart(start);
        return task;
    }
}
