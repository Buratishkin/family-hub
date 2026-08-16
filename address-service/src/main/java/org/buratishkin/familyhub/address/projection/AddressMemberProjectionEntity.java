package org.buratishkin.familyhub.address.projection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "address_member_projection",
        indexes = {
                @Index(name = "idx_address_member_projection_family", columnList = "family_id"),
                @Index(name = "idx_address_member_projection_family_user", columnList = "family_id,user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AddressMemberProjectionEntity {
    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "user_id")
    private Long userId;
}
