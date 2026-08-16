package org.buratishkin.familyhub.notification.projection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationUserProjectionRepository extends JpaRepository<NotificationUserProjectionEntity, Long> {
    Optional<NotificationUserProjectionEntity> findByUserId(Long userId);

    Optional<NotificationUserProjectionEntity> findByUsername(String username);
}
