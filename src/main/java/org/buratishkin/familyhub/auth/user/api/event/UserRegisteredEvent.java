package org.buratishkin.familyhub.auth.user.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record UserRegisteredEvent(
        Long userId,
        String username,
        String email,
        LocalDateTime occurredAt
) implements DomainEvent {
}
