package org.buratishkin.familyhub.notification.dto;

import java.util.UUID;

public record CreateNotificationCommand(
        Long familyId,
        String type,
        String title,
        String body,
        UUID sourceEventId,
        String sourceEventType,
        String aggregateType,
        String aggregateId
) {
}
