package org.buratishkin.familyhub.family.plan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FamilyPlanRepository extends JpaRepository<FamilyPlanEntity, Long> {
    List<FamilyPlanEntity> findAllByFamilyIdAndBusyFromLessThanAndBusyToGreaterThanOrderByBusyFromAscIdAsc(
            Long familyId,
            LocalDateTime to,
            LocalDateTime from
    );

    List<FamilyPlanEntity> findAllByFamilyIdAndMemberIdAndBusyFromLessThanAndBusyToGreaterThanOrderByBusyFromAscIdAsc(
            Long familyId,
            Long memberId,
            LocalDateTime to,
            LocalDateTime from
    );

    List<FamilyPlanEntity> findAllByFamilyIdAndMemberIdAndBusyFromLessThanEqualAndBusyToGreaterThanOrderByBusyFromAscIdAsc(
            Long familyId,
            Long memberId,
            LocalDateTime startInclusive,
            LocalDateTime startExclusive
    );

    Optional<FamilyPlanEntity> findByIdAndFamilyId(Long id, Long familyId);
}
