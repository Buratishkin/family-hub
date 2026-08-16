package org.buratishkin.familyhub.family.alias;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.buratishkin.familyhub.family.member.MemberEntity;

// таблица для кастомных названий участников семьи
@Entity
@Getter
@Setter
public class AliasEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    java.lang.Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_person_id", nullable = false)
    MemberEntity ownerPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_person_id", nullable = false)
    MemberEntity targetPerson;

    @Column(nullable = false)
    String alias;
}
