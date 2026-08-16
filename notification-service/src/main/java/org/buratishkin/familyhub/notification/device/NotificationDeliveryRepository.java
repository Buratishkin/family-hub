package org.buratishkin.familyhub.notification.device;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDeliveryEntity, Long> {
    List<NotificationDeliveryEntity> findByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(
            Collection<DeliveryStatus> statuses,
            int attempts,
            Pageable pageable
    );
}
