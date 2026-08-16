package org.buratishkin.familyhub.family.invite.dto;

import java.time.LocalDateTime;

public record InviteCreateResp(
        Long inviteId,
        String code,
        String deepLink,
        String universalLink,
        LocalDateTime expiresAt
) {
}
