package org.buratishkin.familyhub.shared.outbox;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxDomainEventPublisher implements DomainEventPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(DomainEvent event) {
        LocalDateTime now = LocalDateTime.now();

        OutboxEventEntity outboxEvent = new OutboxEventEntity();
        outboxEvent.setEventId(UUID.randomUUID());
        outboxEvent.setEventType(event.getClass().getName());
        outboxEvent.setAggregateType(OutboxEventMetadata.aggregateType(event));
        outboxEvent.setAggregateId(OutboxEventMetadata.aggregateId(event));
        outboxEvent.setPayload(toPayload(event));
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setOccurredAt(event.occurredAt());
        outboxEvent.setCreatedAt(now);
        outboxEvent.setNextAttemptAt(now);
        outboxEventRepository.save(outboxEvent);
    }

    private String toPayload(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize domain event " + event.getClass().getName(), e);
        }
    }
}
