package org.buratishkin.familyhub.notification.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "notification_user_projection",
        indexes = @Index(name = "idx_notification_user_projection_username", columnList = "username"),
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_user_projection_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_notification_user_projection_username", columnNames = "username")
        }
)
@Getter
@Setter
public class NotificationUserProjectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String username;

    @Column
    private String email;
}
