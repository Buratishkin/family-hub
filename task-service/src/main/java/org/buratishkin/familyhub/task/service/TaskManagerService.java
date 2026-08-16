package org.buratishkin.familyhub.task.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buratishkin.familyhub.address.api.AddressView;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.api.event.TaskAssignedEvent;
import org.buratishkin.familyhub.task.api.event.TaskCreatedEvent;
import org.buratishkin.familyhub.task.api.event.TaskDeletedEvent;
import org.buratishkin.familyhub.task.api.event.TaskUpdatedEvent;
import org.buratishkin.familyhub.task.dto.TaskCreateReq;
import org.buratishkin.familyhub.task.dto.TaskCreateResp;
import org.buratishkin.familyhub.task.dto.TaskCreateResultResp;
import org.buratishkin.familyhub.task.dto.TaskDeleteReq;
import org.buratishkin.familyhub.task.dto.TaskScheduleConflictResp;
import org.buratishkin.familyhub.task.dto.TaskUpdateResultResp;
import org.buratishkin.familyhub.task.dto.TaskUpdateReq;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import org.buratishkin.familyhub.task.mapper.TaskMapper;
import org.buratishkin.familyhub.task.port.TaskAddressLookupPort;
import org.buratishkin.familyhub.task.port.TaskFamilyAccessPort;
import org.buratishkin.familyhub.task.port.TaskMemberLookupPort;
import org.buratishkin.familyhub.task.recurrence.TaskRecurrenceService;
import org.buratishkin.familyhub.task.reminder.TaskReminderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagerService {
    private final TaskMemberLookupPort memberLookupPort;
    private final TaskAddressLookupPort addressLookupPort;
    private final TaskMapper taskMapper;
    private final TaskCrudService taskCrudService;
    private final TaskFamilyAccessPort familyAccessPort;
    private final DomainEventPublisher domainEventPublisher;
    private final TaskRecurrenceService recurrenceService;
    private final TaskReminderService reminderService;
    private final TaskScheduleConflictService scheduleConflictService;

    @Transactional
    public TaskCreateResultResp create(TaskCreateReq createReq, Authentication authentication){
        UserView user = familyAccessPort.currentUser(authentication.getName());
        Long familyId = createReq.familyId();

        TaskCreateResultResp access = validateFamilyAccess(user.id(), familyId, this::getCreateFalseResult);
        if (access != null) {
            return access;
        }

        MemberView creator = memberLookupPort.findMemberByFamilyIdAndUserId(familyId, user.id());

        MemberView assignee = null;
        if (createReq.assigneeId() != null) {
            assignee = memberLookupPort.findMemberById(createReq.assigneeId());
            if (!isMemberBelongsToFamily(assignee, familyId)) {
                return getCreateFalseResult();
            }
        }

        AddressView address = null;
        if (createReq.addressId() != null) {
            address = addressLookupPort.findAddressById(createReq.addressId());
            if (!isAddressBelongsToFamily(address, familyId)) {
                return getCreateFalseResult();
            }
        }
        if (!recurrenceService.isValid(createReq.recurrence(), createReq.start())) {
            return getCreateInvalidRecurrenceResult();
        }

        List<TaskScheduleConflictResp> conflicts = scheduleConflictService.findConflicts(
                familyId,
                assignee == null ? null : assignee.id(),
                createReq.start(),
                null
        );
        if (!conflicts.isEmpty()) {
            return getCreateScheduleConflictResult(conflicts);
        }

        TaskEntity task = taskMapper.toEntity(createReq, creator.id(), assignee == null ? null : assignee.id());
        task.setAddressId(address == null ? null : address.id());
        taskCrudService.save(task);
        safePublish(new TaskCreatedEvent(
                task.getId(),
                familyId,
                creator.id(),
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
        Long recurrenceSeriesId = null;
        if (createReq.recurrence() != null) {
            recurrenceSeriesId = recurrenceService.createSeries(task, familyId, creator.id(), createReq.recurrence()).getId();
        } else {
            reminderService.scheduleForTask(familyId, task);
        }
        return getCreateTrueResult(new TaskCreateResp(task.getId(), recurrenceSeriesId));
    }

    @Transactional
    public TaskUpdateResultResp update(TaskUpdateReq updateReq, Long taskId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        Long familyId = updateReq.familyId();

        TaskUpdateResultResp access = validateFamilyAccess(user.id(), familyId, this::getUpdateFalseResult);
        if (access != null) {
            return access;
        }

        TaskEntity task = taskCrudService.findById(taskId);
        if (!isTaskBelongsToFamily(task, familyId)) {
            return getUpdateFalseResult();
        }

        MemberView assignee = null;
        if (updateReq.assigneeId() != null) {
            assignee = memberLookupPort.findMemberById(updateReq.assigneeId());
            if (!isMemberBelongsToFamily(assignee, familyId)) {
                return getUpdateFalseResult();
            }
        }

        AddressView address = null;
        if (updateReq.addressId() != null) {
            address = addressLookupPort.findAddressById(updateReq.addressId());
            if (!isAddressBelongsToFamily(address, familyId)) {
                return getUpdateFalseResult();
            }
        }

        Long previousAssigneeId = task.getAssigneeId();
        var previousStatus = task.getStatus();

        Long effectiveAssigneeId = updateReq.assigneeId() == null
                ? task.getAssigneeId()
                : assignee == null ? null : assignee.id();
        LocalDateTime effectiveStart = updateReq.start() == null ? task.getStart() : updateReq.start();
        TaskStatusEnum effectiveStatus = updateReq.status() == null ? task.getStatus() : updateReq.status();
        if (scheduleConflictService.isActive(effectiveStatus)) {
            List<TaskScheduleConflictResp> conflicts = scheduleConflictService.findConflicts(
                    familyId,
                    effectiveAssigneeId,
                    effectiveStart,
                    taskId
            );
            if (!conflicts.isEmpty()) {
                return getUpdateScheduleConflictResult(conflicts);
            }
        }

        taskMapper.applyUpdate(updateReq, task, assignee == null ? null : assignee.id(), address == null ? null : address.id());
        taskCrudService.save(task);
        reminderService.scheduleForTask(familyId, task);
        safePublish(new TaskUpdatedEvent(
                task.getId(),
                familyId,
                user.id(),
                task.getName(),
                LocalDateTime.now()
        ));
        if (updateReq.assigneeId() != null && !Objects.equals(previousAssigneeId, task.getAssigneeId())) {
            safePublish(new TaskAssignedEvent(
                    task.getId(),
                    familyId,
                    previousAssigneeId,
                    task.getAssigneeId(),
                    task.getName(),
                    LocalDateTime.now()
            ));
        }
        TaskUpdateResultResp.NextTaskResp nextTask = previousStatus != task.getStatus()
                && task.getStatus() == org.buratishkin.familyhub.task.enums.TaskStatusEnum.FINISHED
                ? recurrenceService.generateAfterFinish(task)
                : null;
        return getUpdateTrueResult(nextTask);
    }

    @Transactional
    public DeleteResp delete(TaskDeleteReq deleteReq, Long taskId, Authentication authentication) {
        UserView user = familyAccessPort.currentUser(authentication.getName());
        Long familyId = deleteReq.familyId();

        DeleteResp access = validateFamilyAccess(user.id(), familyId, this::getDeleteFalseResult);
        if (access != null) {
            return access;
        }

        TaskEntity task = taskCrudService.findById(taskId);
        if (!isTaskBelongsToFamily(task, familyId)) {
            return getDeleteFalseResult();
        }

        String taskName = task.getName();
        reminderService.cancelPending(taskId);
        taskCrudService.delete(task);
        safePublish(new TaskDeletedEvent(
                taskId,
                familyId,
                user.id(),
                taskName,
                LocalDateTime.now()
        ));
        return getDeleteTrueResult();
    }

    private void safePublish(DomainEvent event) {
        try {
            domainEventPublisher.publish(event);
        } catch (RuntimeException e) {
            log.warn("Failed to publish task domain event {}", event.getClass().getName(), e);
        }
    }

    private boolean isMemberBelongsToFamily(MemberView member, Long familyId) {
        return member != null && familyId.equals(member.familyId());
    }

    private boolean isAddressBelongsToFamily(AddressView address, Long familyId) {
        return address != null && familyId.equals(address.familyId());
    }

    private boolean isTaskBelongsToFamily(TaskEntity task, Long familyId) {
        if (task.getCreatorId() == null) {
            return false;
        }
        MemberView creator = memberLookupPort.findMemberById(task.getCreatorId());
        return isMemberBelongsToFamily(creator, familyId);
    }

    private <T> T validateFamilyAccess(Long userId, Long familyId, Supplier<T> falseResultSupplier) {
        if (!familyAccessPort.hasFamilyAccess(userId, familyId)) {
            return falseResultSupplier.get();
        }
        return null;
    }

    private TaskCreateResultResp getCreateFalseResult() {
        return new TaskCreateResultResp(
                false,
                "invalid familyId",
                null,
                List.of()
        );
    }

    private TaskCreateResultResp getCreateInvalidRecurrenceResult() {
        return new TaskCreateResultResp(
                false,
                "invalid recurrence",
                null,
                List.of()
        );
    }

    private TaskCreateResultResp getCreateScheduleConflictResult(List<TaskScheduleConflictResp> conflicts) {
        return new TaskCreateResultResp(
                false,
                "schedule conflict",
                null,
                conflicts
        );
    }

    private TaskCreateResultResp getCreateTrueResult(TaskCreateResp createResp) {
        return new TaskCreateResultResp(
                true,
                "good data",
                createResp,
                List.of()
        );
    }

    private TaskUpdateResultResp getUpdateFalseResult() {
        return new TaskUpdateResultResp(
                false,
                "invalid familyId",
                null,
                List.of()
        );
    }

    private TaskUpdateResultResp getUpdateScheduleConflictResult(List<TaskScheduleConflictResp> conflicts) {
        return new TaskUpdateResultResp(
                false,
                "schedule conflict",
                null,
                conflicts
        );
    }

    private TaskUpdateResultResp getUpdateTrueResult(TaskUpdateResultResp.NextTaskResp nextTask) {
        return new TaskUpdateResultResp(
                true,
                "good data",
                nextTask,
                List.of()
        );
    }

    private DeleteResp getDeleteFalseResult() {
        return new DeleteResp(
                false,
                "invalid familyId"
        );
    }

    private DeleteResp getDeleteTrueResult() {
        return new DeleteResp(
                true,
                "good data"
        );
    }
}
