package org.buratishkin.familyhub.address.projection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressUserProjectionRepository extends JpaRepository<AddressUserProjectionEntity, Long> {
    Optional<AddressUserProjectionEntity> findByUsername(String username);
}
