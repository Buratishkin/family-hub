package org.buratishkin.familyhub.family.member.api;

import org.buratishkin.familyhub.family.member.enums.MemberRole;

import java.time.LocalDateTime;

public record MemberView(
        Long id,
        Long familyId,
        Long userId,
        String username,
        MemberRole role,
        String defaultName,
        LocalDateTime joinTime
) {
}
