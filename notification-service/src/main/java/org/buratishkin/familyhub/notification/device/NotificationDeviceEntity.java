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
        name = "notification_devices",
        indexes = @Index(name = "idx_notification_devices_user_active", columnList = "user_id,active"),
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_devices_user_token", columnNames = {"user_id", "device_token"})
)
@Getter
@Setter
public class NotificationDeviceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_token", nullable = false, length = 1024)
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DevicePlatform platform = DevicePlatform.UNKNOWN;

    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;
}
