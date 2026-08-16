package org.buratishkin.familyhub.task;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.buratishkin.familyhub.task.enums.TaskEnum;
import org.buratishkin.familyhub.task.enums.TaskStatusEnum;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private String name;

    private String description;
    private LocalDateTime start;

    @Column(name = "address_id")
    private Long addressId;

    @Enumerated(value = EnumType.STRING)
    private TaskEnum type;

    @Enumerated(value = EnumType.STRING)
    private TaskStatusEnum status;

    @Column(name = "recurrence_series_id")
    private Long recurrenceSeriesId;

    @Column(name = "recurrence_root_task_id")
    private Long recurrenceRootTaskId;

    @Column(name = "recurrence_parent_task_id")
    private Long recurrenceParentTaskId;

    @Column(name = "recurrence_index")
    private Integer recurrenceIndex;
}
