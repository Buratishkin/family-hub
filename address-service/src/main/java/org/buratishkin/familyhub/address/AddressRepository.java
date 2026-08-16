package org.buratishkin.familyhub.address;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    List<AddressEntity> findAllByFamilyId(Long familyId);
}
