package org.buratishkin.familyhub.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);

    List<NotificationEntity> findByRecipientUserIdAndStatusOrderByCreatedAtDesc(
            Long recipientUserId,
            NotificationStatus status,
            Pageable pageable
    );

    List<NotificationEntity> findByRecipientUserIdAndStatus(Long recipientUserId, NotificationStatus status);

    List<NotificationEntity> findByRecipientUserIdAndStatusOrderByCreatedAtAsc(Long recipientUserId, NotificationStatus status);

    boolean existsBySourceEventIdAndRecipientUserIdAndType(UUID sourceEventId, Long recipientUserId, String type);

    long countByRecipientUserIdAndStatus(Long recipientUserId, NotificationStatus status);
}
