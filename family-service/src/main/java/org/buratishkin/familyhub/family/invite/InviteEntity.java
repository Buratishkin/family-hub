package org.buratishkin.familyhub.family.invite;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.buratishkin.familyhub.family.FamilyEntity;
import org.buratishkin.familyhub.family.member.MemberEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_invite")
@NoArgsConstructor
@Getter
@Setter
public class InviteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private FamilyEntity family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_member_id", nullable = false)
    private MemberEntity createdBy;

    @Column(nullable = false, length = 64)
    private String codeHash;

    @Column(nullable = false, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
