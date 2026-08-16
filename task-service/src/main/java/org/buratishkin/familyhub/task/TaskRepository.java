package org.buratishkin.familyhub.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    @Query("""
            select task from TaskEntity task
            where task.creatorId in :creatorIds
              and task.status in :currentStatuses
              and (task.start is null or task.start >= :now)
            """)
    List<TaskEntity> findCurrentByCreatorIdIn(@Param("creatorIds") Collection<Long> creatorIds,
                                              @Param("currentStatuses") Collection<TaskStatusEnum> currentStatuses,
                                              @Param("now") LocalDateTime now);

    @Query("""
            select task from TaskEntity task
            where task.creatorId in :creatorIds
              and (
                task.status in :oldStatuses
                or (task.start is not null and task.start < :now)
              )
            """)
    List<TaskEntity> findOldByCreatorIdIn(@Param("creatorIds") Collection<Long> creatorIds,
                                          @Param("oldStatuses") Collection<TaskStatusEnum> oldStatuses,
                                          @Param("now") LocalDateTime now);

    int countByAssigneeId(Long assigneeId);

    int countByCreatorId(Long creatorId);

    long countByRecurrenceSeriesIdAndStatusIn(Long recurrenceSeriesId, Collection<TaskStatusEnum> statuses);

    List<TaskEntity> findAllByRecurrenceSeriesIdAndStatusIn(Long recurrenceSeriesId, Collection<TaskStatusEnum> statuses);

    @Query("""
            select task from TaskEntity task
            where task.assigneeId = :assigneeId
              and task.start = :start
              and task.status in :activeStatuses
              and (:excludedTaskId is null or task.id <> :excludedTaskId)
            order by task.id asc
            """)
    List<TaskEntity> findScheduleConflicts(@Param("assigneeId") Long assigneeId,
                                           @Param("start") LocalDateTime start,
                                           @Param("activeStatuses") Collection<TaskStatusEnum> activeStatuses,
                                           @Param("excludedTaskId") Long excludedTaskId);

    List<TaskEntity> findAllByRecurrenceSeriesIdAndRecurrenceIndexGreaterThanEqualOrderByRecurrenceIndexAsc(
            Long recurrenceSeriesId,
            Integer recurrenceIndex
    );
}
