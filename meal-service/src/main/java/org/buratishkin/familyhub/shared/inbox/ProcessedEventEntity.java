package org.buratishkin.familyhub.shared.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "processed_events",
        indexes = @Index(name = "idx_processed_events_aggregate", columnList = "aggregate_type,aggregate_id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_processed_events_event_id", columnNames = "event_id")
)
@Getter
@Setter
@NoArgsConstructor
public class ProcessedEventEntity {
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

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
