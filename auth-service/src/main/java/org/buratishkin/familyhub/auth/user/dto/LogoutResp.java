package org.buratishkin.familyhub.auth.user.dto;

public record LogoutResp(
        boolean result,
        String reason
) {
}
