package org.buratishkin.familyhub.family.projection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyUserProjectionRepository extends JpaRepository<FamilyUserProjectionEntity, Long> {
    Optional<FamilyUserProjectionEntity> findByUsername(String username);
}
