package org.buratishkin.familyhub.notification.dto;

import org.buratishkin.familyhub.notification.NotificationEntity;
import org.buratishkin.familyhub.notification.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationResp(
        Long id,
        Long familyId,
        String type,
        String title,
        String body,
        NotificationStatus status,
        String aggregateType,
        String aggregateId,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
    public static NotificationResp from(NotificationEntity notification) {
        return new NotificationResp(
                notification.getId(),
                notification.getFamilyId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getStatus(),
                notification.getAggregateType(),
                notification.getAggregateId(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
