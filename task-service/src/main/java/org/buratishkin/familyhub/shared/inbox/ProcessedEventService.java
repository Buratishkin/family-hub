package org.buratishkin.familyhub.shared.inbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProcessedEventService {
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public boolean processOnce(KafkaDomainEventMessage message, Runnable handler) {
        if (processedEventRepository.existsByEventId(message.eventId())) {
            return false;
        }

        ProcessedEventEntity processedEvent = new ProcessedEventEntity();
        processedEvent.setEventId(message.eventId());
        processedEvent.setEventType(message.eventType());
        processedEvent.setAggregateType(message.aggregateType());
        processedEvent.setAggregateId(message.aggregateId());
        processedEvent.setProcessedAt(LocalDateTime.now());
        processedEventRepository.saveAndFlush(processedEvent);

        handler.run();
        return true;
    }
}
