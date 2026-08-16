package org.buratishkin.familyhub.family.poll;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "family_poll_answers",
        indexes = @Index(name = "idx_family_poll_answers_poll_created", columnList = "poll_id,created_at")
)
@Getter
@Setter
@NoArgsConstructor
public class PollAnswerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private PollEntity poll;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 4000)
    private String text;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
