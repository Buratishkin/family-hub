package org.buratishkin.familyhub.task.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.TaskRepository;
import org.buratishkin.familyhub.task.api.TaskLookupApi;
import org.buratishkin.familyhub.task.api.TaskView;
import org.buratishkin.familyhub.task.enums.TaskEnum;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import org.buratishkin.familyhub.task.exception.TaskNotFoundException;
import org.buratishkin.familyhub.task.port.TaskMemberLookupPort;
import org.buratishkin.familyhub.task.recurrence.TaskRecurrenceService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskCrudService implements TaskLookupApi {
    private static final List<TaskStatusEnum> CURRENT_STATUSES = List.of(
            TaskStatusEnum.SCHEDULED,
            TaskStatusEnum.IN_PROGRESS
    );
    private static final List<TaskStatusEnum> OLD_STATUSES = List.of(
            TaskStatusEnum.FINISHED,
            TaskStatusEnum.CANCELED
    );

    private final TaskRepository taskRepository;
    private final TaskMemberLookupPort memberLookupPort;
    private final TaskRecurrenceService recurrenceService;

    public TaskEntity findById(Long taskId){
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Нет задачи с id: " + taskId));
    }

    public TaskEntity cancelTask(Long id){
        return changeTaskStatus(id, TaskStatusEnum.CANCELED);
    }

    public TaskEntity finishTask(Long id){
        return changeTaskStatus(id, TaskStatusEnum.FINISHED);
    }

    private TaskEntity changeTaskStatus(Long id, TaskStatusEnum status){
        TaskEntity task = findById(id);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    public TaskEntity changeName(Long familyId, String name){
        TaskEntity task = findById(familyId);
        task.setName(name);
        return save(task);
    }

    public TaskEntity changeDescription(Long familyId, String description){
        TaskEntity task = findById(familyId);
        task.setDescription(description);
        return save(task);
    }

    public TaskEntity changeStarTime(Long familyId, LocalDateTime startTime){
        TaskEntity task = findById(familyId);
        task.setStart(startTime);
        return save(task);
    }

    public TaskEntity changeType(Long familyId, TaskEnum type){
        TaskEntity task = findById(familyId);
        task.setType(type);
        return save(task);
    }

    public TaskEntity save(TaskEntity task){
        return taskRepository.save(task);
    }

    public void delete(TaskEntity task) {
        taskRepository.delete(task);
    }

    public List<TaskEntity> findAllByFamilyId(Long familyId) {
        List<Long> memberIds = memberLookupPort.findMembersByFamilyId(familyId).stream()
                .map(MemberView::id)
                .toList();
        if (memberIds.isEmpty()) {
            return List.of();
        }
        return taskRepository.findCurrentByCreatorIdIn(memberIds, CURRENT_STATUSES, LocalDateTime.now());
    }

    public List<TaskEntity> findOldByFamilyId(Long familyId) {
        List<Long> memberIds = memberLookupPort.findMembersByFamilyId(familyId).stream()
                .map(MemberView::id)
                .toList();
        if (memberIds.isEmpty()) {
            return List.of();
        }
        return taskRepository.findOldByCreatorIdIn(memberIds, OLD_STATUSES, LocalDateTime.now());
    }

    public List<TaskView> findTasksByFamilyId(Long familyId) {
        return findAllByFamilyId(familyId).stream()
                .map(this::toView)
                .toList();
    }

    public List<TaskView> findOldTasksByFamilyId(Long familyId) {
        return findOldByFamilyId(familyId).stream()
                .map(this::toView)
                .toList();
    }

    public int countByAssigneeMemberId(Long memberId) {
        return taskRepository.countByAssigneeId(memberId);
    }

    public int countByCreatorMemberId(Long memberId) {
        return taskRepository.countByCreatorId(memberId);
    }

    private TaskView toView(TaskEntity task) {
        if (task == null) {
            return null;
        }
        return new TaskView(
                task.getId(),
                task.getCreatorId() == null ? null : memberLookupPort.findMemberById(task.getCreatorId()).familyId(),
                task.getCreatorId(),
                task.getAssigneeId(),
                task.getAddressId(),
                task.getName(),
                task.getDescription(),
                task.getStart(),
                task.getType() == null ? null : task.getType().name(),
                task.getStatus() == null ? null : task.getStatus().name(),
                task.getRecurrenceSeriesId(),
                task.getRecurrenceRootTaskId(),
                task.getRecurrenceParentTaskId(),
                task.getRecurrenceIndex(),
                recurrenceService.toView(task.getRecurrenceSeriesId())
        );
    }


}
