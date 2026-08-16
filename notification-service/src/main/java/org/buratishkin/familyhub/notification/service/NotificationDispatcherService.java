package org.buratishkin.familyhub.notification.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.notification.NotificationEntity;
import org.buratishkin.familyhub.notification.NotificationRepository;
import org.buratishkin.familyhub.notification.device.DeliveryStatus;
import org.buratishkin.familyhub.notification.device.NotificationDeliveryEntity;
import org.buratishkin.familyhub.notification.device.NotificationDeliveryRepository;
import org.buratishkin.familyhub.notification.device.NotificationDeviceRepository;
import org.buratishkin.familyhub.notification.push.DevicePushPayload;
import org.buratishkin.familyhub.notification.push.DevicePushResult;
import org.buratishkin.familyhub.notification.push.DevicePushSender;
import org.buratishkin.familyhub.notification.push.InvalidDeviceTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationDispatcherService {
    private static final int BATCH_SIZE = 50;

    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationDeviceRepository deviceRepository;
    private final DevicePushSender pushSender;

    @Value("${family-hub.notification.delivery-max-attempts:3}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${family-hub.notification.dispatch-delay-ms:5000}")
    @Transactional
    public void dispatchPendingDeliveries() {
        List<NotificationDeliveryEntity> deliveries = deliveryRepository
                .findByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(
                        Set.of(DeliveryStatus.PENDING, DeliveryStatus.FAILED),
                        maxAttempts,
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (NotificationDeliveryEntity delivery : deliveries) {
            dispatch(delivery);
        }
    }

    private void dispatch(NotificationDeliveryEntity delivery) {
        try {
            NotificationEntity notification = notificationRepository.findById(delivery.getNotificationId())
                    .orElseThrow(() -> new IllegalStateException("Notification not found: " + delivery.getNotificationId()));
            DevicePushResult result = pushSender.send(delivery.getDeviceTokenSnapshot(), toPayload(notification));
            delivery.setStatus(DeliveryStatus.SENT);
            delivery.setSentAt(LocalDateTime.now());
            delivery.setLastError(null);
            delivery.setProviderMessageId(result.providerMessageId());
        } catch (InvalidDeviceTokenException e) {
            deactivateDevice(delivery.getDeviceId());
            delivery.setStatus(DeliveryStatus.FAILED);
            delivery.setLastError(trim(e.getMessage()));
            delivery.setAttempts(maxAttempts);
        } catch (Exception e) {
            delivery.setStatus(DeliveryStatus.FAILED);
            delivery.setLastError(trim(e.getMessage()));
        } finally {
            delivery.setAttempts(delivery.getAttempts() + 1);
            deliveryRepository.save(delivery);
        }
    }

    private void deactivateDevice(Long deviceId) {
        deviceRepository.findById(deviceId).ifPresent(device -> {
            device.setActive(false);
            device.setUpdatedAt(LocalDateTime.now());
            deviceRepository.save(device);
        });
    }

    private DevicePushPayload toPayload(NotificationEntity notification) {
        return new DevicePushPayload(
                notification.getId(),
                notification.getFamilyId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getAggregateType(),
                notification.getAggregateId()
        );
    }

    private String trim(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }
}
