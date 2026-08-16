package org.buratishkin.familyhub.shared.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
