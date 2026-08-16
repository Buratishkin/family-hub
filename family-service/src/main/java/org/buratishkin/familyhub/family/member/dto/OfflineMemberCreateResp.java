package org.buratishkin.familyhub.family.member.dto;

public record OfflineMemberCreateResp(
        Long memberId,
        Long familyId,
        String name
) {
}
