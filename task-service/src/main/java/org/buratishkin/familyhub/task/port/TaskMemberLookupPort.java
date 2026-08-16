package org.buratishkin.familyhub.task.port;

import org.buratishkin.familyhub.family.member.api.MemberView;

import java.util.List;

public interface TaskMemberLookupPort {
    MemberView findMemberById(Long memberId);

    MemberView findMemberByFamilyIdAndUserId(Long familyId, Long userId);

    List<MemberView> findMembersByFamilyId(Long familyId);
}
