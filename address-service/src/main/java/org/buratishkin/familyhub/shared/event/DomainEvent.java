package org.buratishkin.familyhub.shared.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
}
