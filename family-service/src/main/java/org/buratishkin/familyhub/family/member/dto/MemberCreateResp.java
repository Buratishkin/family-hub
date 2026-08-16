package org.buratishkin.familyhub.family.member.dto;

import org.buratishkin.familyhub.family.member.MemberEntity;

public record MemberCreateResp(
        boolean res,
        String reason,
        MemberEntity member
) {
}
