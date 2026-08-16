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
        name = "notification_member_projection",
        indexes = {
                @Index(name = "idx_notification_member_projection_family_active", columnList = "family_id,active"),
                @Index(name = "idx_notification_member_projection_user", columnList = "user_id")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_member_projection_member_id", columnNames = "member_id")
)
@Getter
@Setter
public class NotificationMemberProjectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private boolean active = true;
}
