package org.buratishkin.familyhub.family.member.dto;

import org.buratishkin.familyhub.family.FamilyEntity;

public record MemberDeleteResp(
        boolean result,
        FamilyEntity family
) {
}
