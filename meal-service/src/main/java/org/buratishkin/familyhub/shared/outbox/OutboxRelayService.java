package org.buratishkin.familyhub.shared.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxRelayService {
    private static final int BATCH_SIZE = 50;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaOutboxEventPublisher kafkaOutboxEventPublisher;

    @Scheduled(fixedDelayString = "${family-hub.outbox.publish-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<OutboxEventEntity> events = outboxEventRepository
                .findByStatusAndNextAttemptAtLessThanEqualOrderByOccurredAtAsc(
                        OutboxEventStatus.PENDING,
                        now,
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (OutboxEventEntity event : events) {
            publish(event, now);
        }
    }

    private void publish(OutboxEventEntity outboxEvent, LocalDateTime now) {
        try {
            kafkaOutboxEventPublisher.publish(outboxEvent);
            outboxEvent.markPublished(now);
        } catch (Exception e) {
            outboxEvent.markFailed(e.getMessage(), now);
        }
    }
}
