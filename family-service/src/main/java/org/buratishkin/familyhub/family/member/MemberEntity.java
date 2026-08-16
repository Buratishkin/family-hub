package org.buratishkin.familyhub.family.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.buratishkin.familyhub.family.alias.AliasEntity;
import org.buratishkin.familyhub.family.FamilyEntity;
import org.buratishkin.familyhub.family.member.enums.MemberRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"family_id", "user_id"}))
@NoArgsConstructor
@Getter
@Setter
public class MemberEntity {

    public MemberEntity(FamilyEntity family, Long userId){
        this.family = family;
        this.userId = userId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private FamilyEntity family;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    private String defaultName;

    @Column(nullable = false)
    private LocalDateTime joinTime;

    @OneToMany(mappedBy = "ownerPerson")
    private List<AliasEntity> aliasOwners = new ArrayList<>();

    @OneToMany(mappedBy = "targetPerson")
    private List<AliasEntity> aliasTargets = new ArrayList<>();
}
