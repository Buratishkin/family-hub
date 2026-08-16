package org.buratishkin.familyhub.notification.device;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_deliveries",
        indexes = @Index(name = "idx_notification_deliveries_status_created", columnList = "status,created_at"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_deliveries_notification_device",
                columnNames = {"notification_id", "device_id"}
        )
)
@Getter
@Setter
public class NotificationDeliveryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "device_token_snapshot", nullable = false, length = 1024)
    private String deviceTokenSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
