package org.buratishkin.familyhub.auth.user.dto;

public record LoginReq(
        String username,
        String password
) {
}
