package org.buratishkin.familyhub.task.recurrence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_recurrence_series")
@Getter
@Setter
public class TaskRecurrenceSeriesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long familyId;

    private Long rootTaskId;

    @Column(nullable = false)
    private Long createdByMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskRecurrenceSeriesStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskRecurrenceFrequency frequency;

    @Column(nullable = false)
    private int intervalValue;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    private LocalDateTime repeatUntil;

    private Integer maxOccurrences;

    @Column(nullable = false)
    private int generatedOccurrencesCount;

    private LocalDateTime cancelledAt;

    private Long cancelledByMemberId;
}
