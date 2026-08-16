package org.buratishkin.familyhub.address.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    CategoryType type;

    @Column(name = "family_id")
    Long ownerFamilyId;

    boolean isArchived;

    Instant createdAt;

}
