package org.buratishkin.familyhub.task.recurrence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRecurrenceSeriesRepository extends JpaRepository<TaskRecurrenceSeriesEntity, Long> {
}
