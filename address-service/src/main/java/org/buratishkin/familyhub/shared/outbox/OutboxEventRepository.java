package org.buratishkin.familyhub.shared.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {
    List<OutboxEventEntity> findByStatusAndNextAttemptAtLessThanEqualOrderByOccurredAtAsc(
            OutboxEventStatus status,
            LocalDateTime nextAttemptAt,
            Pageable pageable
    );
}
