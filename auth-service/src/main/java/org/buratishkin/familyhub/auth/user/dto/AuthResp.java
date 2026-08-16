package org.buratishkin.familyhub.auth.user.dto;

public record AuthResp(
        boolean result,
        String reason,
        Long id,
        String username,
        String email,
        String accessToken,
        String refreshToken
){
}
