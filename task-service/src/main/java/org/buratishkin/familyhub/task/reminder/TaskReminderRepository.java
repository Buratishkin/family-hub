package org.buratishkin.familyhub.task.reminder;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskReminderRepository extends JpaRepository<TaskReminderEntity, Long> {
    List<TaskReminderEntity> findAllByTaskIdAndStatus(Long taskId, TaskReminderStatus status);

    List<TaskReminderEntity> findByStatusAndDueAtLessThanEqualOrderByDueAtAscIdAsc(
            TaskReminderStatus status,
            LocalDateTime dueAt,
            Pageable pageable
    );
}
