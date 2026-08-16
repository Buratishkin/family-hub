package org.buratishkin.familyhub.task.projection;

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
        name = "task_address_projection",
        indexes = @Index(name = "idx_task_address_projection_family", columnList = "family_id")
)
@Getter
@Setter
@NoArgsConstructor
public class TaskAddressProjectionEntity {
    @Id
    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "category_id")
    private Long categoryId;
}
