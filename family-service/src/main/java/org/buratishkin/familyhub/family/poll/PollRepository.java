package org.buratishkin.familyhub.family.poll;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PollRepository extends JpaRepository<PollEntity, Long> {
    @EntityGraph(attributePaths = "answers")
    List<PollEntity> findAllByFamilyIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
            Long familyId,
            PollStatus status,
            LocalDateTime now
    );

    @EntityGraph(attributePaths = "answers")
    Optional<PollEntity> findByIdAndFamilyId(Long id, Long familyId);

    long deleteByExpiresAtBefore(LocalDateTime now);
}
