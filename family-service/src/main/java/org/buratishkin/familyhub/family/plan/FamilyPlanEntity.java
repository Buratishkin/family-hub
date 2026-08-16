package org.buratishkin.familyhub.family.plan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "family_plans",
        indexes = {
                @Index(name = "idx_family_plans_family_time", columnList = "family_id,busy_from,busy_to"),
                @Index(name = "idx_family_plans_member_time", columnList = "member_id,busy_from,busy_to")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class FamilyPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "busy_from", nullable = false)
    private LocalDateTime busyFrom;

    @Column(name = "busy_to", nullable = false)
    private LocalDateTime busyTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
