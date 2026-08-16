package org.buratishkin.familyhub.task.projection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskUserProjectionRepository extends JpaRepository<TaskUserProjectionEntity, Long> {
    Optional<TaskUserProjectionEntity> findByUsername(String username);
}
