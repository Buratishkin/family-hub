package org.buratishkin.familyhub.shared.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_events_status_next_attempt", columnList = "status,next_attempt_at"),
                @Index(name = "idx_outbox_events_aggregate", columnList = "aggregate_type,aggregate_id")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_outbox_events_event_id", columnNames = "event_id")
)
@Getter
@Setter
@NoArgsConstructor
public class OutboxEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    public void markPublished(LocalDateTime now) {
        status = OutboxEventStatus.PUBLISHED;
        publishedAt = now;
        lastError = null;
    }

    public void markFailed(String error, LocalDateTime now) {
        retryCount++;
        status = retryCount >= 10 ? OutboxEventStatus.FAILED : OutboxEventStatus.PENDING;
        lastError = error;
        nextAttemptAt = now.plusSeconds(Math.min(60L * retryCount, 600L));
    }

    public void markPending(LocalDateTime now) {
        status = OutboxEventStatus.PENDING;
        nextAttemptAt = now;
    }
}
