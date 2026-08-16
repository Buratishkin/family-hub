package org.buratishkin.familyhub.family.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, java.lang.Long> {

    boolean existsByFamily_IdAndUserId(java.lang.Long familyId, java.lang.Long userId);
    boolean existsByUserId(java.lang.Long userId);
    boolean existsByIdAndFamily_Id(java.lang.Long memberId, java.lang.Long familyId);

    List<MemberEntity> findAllByUserId(java.lang.Long userId);

    List<MemberEntity> findAllByFamily_Id(java.lang.Long familyId);

    Optional<MemberEntity> findFamilyMemberEntityByFamily_IdAndUserId(java.lang.Long familyId, java.lang.Long userId);
}
