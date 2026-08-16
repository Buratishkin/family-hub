package org.buratishkin.familyhub.address.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "address_family_projection")
@Getter
@Setter
@NoArgsConstructor
public class AddressFamilyProjectionEntity {
    @Id
    @Column(name = "family_id")
    private Long familyId;

    @Column(name = "admin_member_id")
    private Long adminMemberId;

    @Column(name = "admin_user_id")
    private Long adminUserId;
}
