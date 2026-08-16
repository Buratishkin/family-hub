package org.buratishkin.familyhub.family.member.api;

import java.util.List;

public interface MemberLookupApi {
    MemberView findMemberById(Long memberId);

    MemberView findMemberByFamilyIdAndUserId(Long familyId, Long userId);

    List<MemberView> findMembersByUserId(Long userId);

    List<MemberView> findMembersByFamilyId(Long familyId);

    boolean existsByFamilyIdAndUserId(Long familyId, Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByIdAndFamilyId(Long memberId, Long familyId);
}
