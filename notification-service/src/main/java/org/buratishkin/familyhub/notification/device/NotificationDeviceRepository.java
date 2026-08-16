package org.buratishkin.familyhub.notification.device;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NotificationDeviceRepository extends JpaRepository<NotificationDeviceEntity, Long> {
    Optional<NotificationDeviceEntity> findByUserIdAndDeviceToken(Long userId, String deviceToken);

    Optional<NotificationDeviceEntity> findByIdAndUserId(Long id, Long userId);

    List<NotificationDeviceEntity> findAllByUserIdAndActiveTrue(Long userId);

    List<NotificationDeviceEntity> findAllByActiveTrue();

    List<NotificationDeviceEntity> findAllByUserIdInAndActiveTrue(Collection<Long> userIds);
}
