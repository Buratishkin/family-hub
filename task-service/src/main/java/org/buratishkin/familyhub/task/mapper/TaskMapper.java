package org.buratishkin.familyhub.task.mapper;

import org.buratishkin.familyhub.shared.mapper.Mapper;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.task.TaskEntity;
import org.buratishkin.familyhub.task.dto.TaskCreateReq;
import org.buratishkin.familyhub.task.dto.TaskUpdateReq;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMapper implements Mapper<TaskCreateReq, TaskEntity> {

    @Override
    public TaskEntity toEntity(TaskCreateReq createReq) {
        TaskEntity task = new TaskEntity();

        task.setName(createReq.name());

        task.setStart(createReq.start());
        task.setDescription(createReq.description());
        task.setType(createReq.type());
        task.setStatus(TaskStatusEnum.SCHEDULED);
        return task;
    }

    public TaskEntity toEntity(TaskCreateReq createReq, Long creatorId, Long assigneeId){
        TaskEntity task = toEntity(createReq);
        task.setCreatorId(creatorId);
        task.setAssigneeId(assigneeId);
        return task;
    }

    public void applyUpdate(TaskUpdateReq updateReq, TaskEntity task, Long assigneeId, Long addressId) {
        if (updateReq.name() != null && !updateReq.name().isBlank()) {
            task.setName(updateReq.name());
        }
        if (updateReq.description() != null) {
            task.setDescription(updateReq.description());
        }
        if (updateReq.start() != null) {
            task.setStart(updateReq.start());
        }
        if (updateReq.type() != null) {
            task.setType(updateReq.type());
        }
        if (updateReq.status() != null) {
            task.setStatus(updateReq.status());
        }
        if (updateReq.assigneeId() != null) {
            task.setAssigneeId(assigneeId);
        }
        if (updateReq.addressId() != null) {
            task.setAddressId(addressId);
        }
    }
}
