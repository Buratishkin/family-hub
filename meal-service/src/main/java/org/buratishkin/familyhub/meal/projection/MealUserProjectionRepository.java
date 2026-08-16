package org.buratishkin.familyhub.meal.projection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MealUserProjectionRepository extends JpaRepository<MealUserProjectionEntity, Long> {
    Optional<MealUserProjectionEntity> findByUsername(String username);
}
