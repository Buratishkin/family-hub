package org.buratishkin.familyhub.family.poll;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "family_polls",
        indexes = {
                @Index(name = "idx_family_polls_family_status_created", columnList = "family_id,status,created_at"),
                @Index(name = "idx_family_polls_expires_at", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PollEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "creator_member_id", nullable = false)
    private Long creatorMemberId;

    @Column(nullable = false, length = 1000)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PollStatus status = PollStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "poll", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<PollAnswerEntity> answers = new ArrayList<>();
}
