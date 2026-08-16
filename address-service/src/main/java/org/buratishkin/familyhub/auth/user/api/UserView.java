package org.buratishkin.familyhub.auth.user.api;

public record UserView(
        Long id,
        String username,
        String email
) {
}
