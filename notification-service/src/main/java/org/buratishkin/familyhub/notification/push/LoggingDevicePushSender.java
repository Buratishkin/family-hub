package org.buratishkin.familyhub.notification.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "family-hub.notification.push-provider", havingValue = "logging")
@Slf4j
public class LoggingDevicePushSender implements DevicePushSender {
    @Override
    public DevicePushResult send(String deviceToken, DevicePushPayload payload) {
        log.info(
                "Notification delivery prepared: notificationId={} type={} tokenSuffix={}",
                payload.notificationId(),
                payload.type(),
                tokenSuffix(deviceToken)
        );
        return new DevicePushResult("logging-" + payload.notificationId());
    }

    private String tokenSuffix(String deviceToken) {
        if (deviceToken == null || deviceToken.length() <= 6) {
            return "******";
        }
        return "******" + deviceToken.substring(deviceToken.length() - 6);
    }
}
