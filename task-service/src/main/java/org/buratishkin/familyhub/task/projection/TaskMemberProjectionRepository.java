package org.buratishkin.familyhub.task.projection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskMemberProjectionRepository extends JpaRepository<TaskMemberProjectionEntity, Long> {
    Optional<TaskMemberProjectionEntity> findByFamilyIdAndUserId(Long familyId, Long userId);

    List<TaskMemberProjectionEntity> findAllByFamilyId(Long familyId);

    boolean existsByFamilyIdAndUserId(Long familyId, Long userId);

    void deleteAllByFamilyId(Long familyId);
}
