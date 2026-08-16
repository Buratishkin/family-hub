package org.buratishkin.familyhub.family.invite;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.buratishkin.familyhub.family.member.MemberEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_invite_redemption")
@NoArgsConstructor
@Getter
@Setter
public class InviteRedemptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_id", nullable = false)
    private InviteEntity invite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime redeemedAt;
}
