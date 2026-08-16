package org.buratishkin.familyhub.address.projection;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressMemberProjectionRepository extends JpaRepository<AddressMemberProjectionEntity, Long> {
    boolean existsByFamilyIdAndUserId(Long familyId, Long userId);

    void deleteAllByFamilyId(Long familyId);
}
