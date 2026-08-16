package org.buratishkin.familyhub.notification.push;

public interface DevicePushSender {
    DevicePushResult send(String deviceToken, DevicePushPayload payload);
}
