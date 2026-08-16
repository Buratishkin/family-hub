package org.buratishkin.familyhub.notification.projection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationMemberProjectionRepository extends JpaRepository<NotificationMemberProjectionEntity, Long> {
    Optional<NotificationMemberProjectionEntity> findByMemberId(Long memberId);

    List<NotificationMemberProjectionEntity> findAllByFamilyIdAndActiveTrue(Long familyId);
}
