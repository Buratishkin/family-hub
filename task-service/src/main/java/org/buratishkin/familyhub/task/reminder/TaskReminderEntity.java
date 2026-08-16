package org.buratishkin.familyhub.task.reminder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "task_reminders",
        indexes = {
                @Index(name = "idx_task_reminders_status_due", columnList = "status,due_at"),
                @Index(name = "idx_task_reminders_task_status", columnList = "task_id,status")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TaskReminderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "creator_member_id", nullable = false)
    private Long creatorMemberId;

    @Column(name = "assignee_member_id")
    private Long assigneeMemberId;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "task_start", nullable = false)
    private LocalDateTime taskStart;

    @Column(name = "reminder_minutes", nullable = false)
    private Integer reminderMinutes;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskReminderStatus status = TaskReminderStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
}
