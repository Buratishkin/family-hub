package org.buratishkin.familyhub.notification.push;

public record DevicePushPayload(
        Long notificationId,
        Long familyId,
        String type,
        String title,
        String body,
        String aggregateType,
        String aggregateId
) {
}
