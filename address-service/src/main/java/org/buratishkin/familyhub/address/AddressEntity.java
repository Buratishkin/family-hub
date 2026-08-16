package org.buratishkin.familyhub.address;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.buratishkin.familyhub.address.enums.StreetType;

@Entity
@Getter
@Setter
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String country;

    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StreetType streetType;

    @Column(nullable = false)
    private String street;

    private String house;
    private String apartment;
    private String comment;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;
}
