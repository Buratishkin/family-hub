package org.buratishkin.familyhub.task.projection;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAddressProjectionRepository extends JpaRepository<TaskAddressProjectionEntity, Long> {
    void deleteAllByFamilyId(Long familyId);
}
