package org.buratishkin.familyhub.notification.dto;

import org.buratishkin.familyhub.notification.device.DevicePlatform;
import org.buratishkin.familyhub.notification.device.NotificationDeviceEntity;

import java.time.LocalDateTime;

public record DeviceResp(
        Long id,
        DevicePlatform platform,
        String displayName,
        boolean active,
        LocalDateTime lastSeenAt
) {
    public static DeviceResp from(NotificationDeviceEntity device) {
        return new DeviceResp(
                device.getId(),
                device.getPlatform(),
                device.getDisplayName(),
                device.isActive(),
                device.getLastSeenAt()
        );
    }
}
