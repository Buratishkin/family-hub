package org.buratishkin.familyhub.notification.dto;

import java.util.List;

public record NotificationListResp(
        long unreadCount,
        List<NotificationResp> items
) {
}
