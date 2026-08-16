package org.buratishkin.familyhub.notification.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.notification.NotificationEntity;
import org.buratishkin.familyhub.notification.NotificationRepository;
import org.buratishkin.familyhub.notification.NotificationStatus;
import org.buratishkin.familyhub.notification.device.NotificationDeliveryEntity;
import org.buratishkin.familyhub.notification.device.NotificationDeliveryRepository;
import org.buratishkin.familyhub.notification.device.NotificationDeviceEntity;
import org.buratishkin.familyhub.notification.device.NotificationDeviceRepository;
import org.buratishkin.familyhub.notification.dto.CreateNotificationCommand;
import org.buratishkin.familyhub.notification.dto.NotificationListResp;
import org.buratishkin.familyhub.notification.dto.NotificationResp;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final NotificationRepository notificationRepository;
    private final NotificationDeviceRepository deviceRepository;
    private final NotificationDeliveryRepository deliveryRepository;

    public NotificationListResp list(Long userId, boolean unreadOnly, Integer limit) {
        int pageSize = normalizeLimit(limit);
        List<NotificationEntity> notifications = unreadOnly
                ? notificationRepository.findByRecipientUserIdAndStatusOrderByCreatedAtDesc(
                        userId,
                        NotificationStatus.UNREAD,
                        PageRequest.of(0, pageSize)
                )
                : notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, pageSize));

        long unreadCount = notificationRepository.countByRecipientUserIdAndStatus(userId, NotificationStatus.UNREAD);
        return new NotificationListResp(
                unreadCount,
                notifications.stream().map(NotificationResp::from).toList()
        );
    }

    @Transactional
    public NotificationResp markRead(Long userId, Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .filter(item -> item.getRecipientUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (notification.getStatus() == NotificationStatus.UNREAD) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return NotificationResp.from(notification);
    }

    @Transactional
    public void markAllRead(Long userId) {
        for (NotificationEntity notification : notificationRepository.findByRecipientUserIdAndStatus(
                userId,
                NotificationStatus.UNREAD
        )) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void createForUsers(Collection<Long> recipientUserIds, CreateNotificationCommand command) {
        Set<Long> distinctRecipients = new LinkedHashSet<>(recipientUserIds);
        distinctRecipients.remove(null);

        for (Long recipientUserId : distinctRecipients) {
            if (notificationRepository.existsBySourceEventIdAndRecipientUserIdAndType(
                    command.sourceEventId(),
                    recipientUserId,
                    command.type()
            )) {
                continue;
            }

            NotificationEntity notification = new NotificationEntity();
            notification.setRecipientUserId(recipientUserId);
            notification.setFamilyId(command.familyId());
            notification.setType(command.type());
            notification.setTitle(command.title());
            notification.setBody(command.body());
            notification.setSourceEventId(command.sourceEventId());
            notification.setSourceEventType(command.sourceEventType());
            notification.setAggregateType(command.aggregateType());
            notification.setAggregateId(command.aggregateId());
            notification.setCreatedAt(LocalDateTime.now());

            NotificationEntity saved = notificationRepository.save(notification);
            createDeliveries(saved, deviceRepository.findAllByUserIdAndActiveTrue(recipientUserId));
        }
    }

    private void createDeliveries(NotificationEntity notification, List<NotificationDeviceEntity> devices) {
        LocalDateTime now = LocalDateTime.now();
        for (NotificationDeviceEntity device : devices) {
            NotificationDeliveryEntity delivery = new NotificationDeliveryEntity();
            delivery.setNotificationId(notification.getId());
            delivery.setDeviceId(device.getId());
            delivery.setDeviceTokenSnapshot(device.getDeviceToken());
            delivery.setCreatedAt(now);
            deliveryRepository.save(delivery);
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
