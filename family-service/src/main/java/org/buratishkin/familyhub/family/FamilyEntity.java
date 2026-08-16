package org.buratishkin.familyhub.family;

import jakarta.persistence.*;
import lombok.*;
import org.buratishkin.familyhub.family.member.MemberEntity;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
public class FamilyEntity {
    // ------------------------------------------------------------- //
    // при добавлении новых полей их надо добавить в FamilyCreateReq //
    // ------------------------------------------------------------- //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Long id;

    @Column(nullable = false)
    private String name;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private MemberEntity admin;

    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval=true)
    private List<MemberEntity> members = new ArrayList<>();

}
