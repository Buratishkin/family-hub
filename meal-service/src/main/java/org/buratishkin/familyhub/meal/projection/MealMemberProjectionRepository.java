package org.buratishkin.familyhub.meal.projection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MealMemberProjectionRepository extends JpaRepository<MealMemberProjectionEntity, Long> {
    Optional<MealMemberProjectionEntity> findByFamilyIdAndUserId(Long familyId, Long userId);

    boolean existsByFamilyIdAndUserId(Long familyId, Long userId);

    void deleteAllByFamilyId(Long familyId);
}
