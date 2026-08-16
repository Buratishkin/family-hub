package org.buratishkin.familyhub.family.projection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.buratishkin.familyhub.shared.event.DomainEventEnvelope;
import org.buratishkin.familyhub.shared.inbox.KafkaDomainEventMessage;
import org.buratishkin.familyhub.shared.inbox.ProcessedEventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FamilyDependencyProjectionConsumer {
    private static final String USER_REGISTERED = "org.buratishkin.familyhub.auth.user.api.event.UserRegisteredEvent";

    private final ProcessedEventService processedEventService;
    private final FamilyDependencyProjectionService projectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${family-hub.outbox.topic:familyhub.domain-events}",
            groupId = "${family-hub.kafka.consumer.family-projections.group-id:family-service-family-projections}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        KafkaDomainEventMessage message = toMessage(record);
        processedEventService.processOnce(message, () -> apply(message));
    }

    private void apply(KafkaDomainEventMessage message) {
        try {
            switch (message.eventType()) {
                case USER_REGISTERED -> applyUserRegistered(message.payload());
                default -> log.debug("Family dependency projection ignored eventType={}", message.eventType());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to apply family dependency projection event " + message.eventType(), e);
        }
    }

    private void applyUserRegistered(String payload) throws Exception {
        UserRegisteredPayload event = objectMapper.readValue(payload, UserRegisteredPayload.class);
        projectionService.applyUserRegistered(event.userId(), event.username(), event.email());
    }

    private KafkaDomainEventMessage toMessage(ConsumerRecord<String, String> record) {
        DomainEventEnvelope envelope = readEnvelope(record.value());
        if (envelope != null) {
            return new KafkaDomainEventMessage(
                    envelope.eventId(),
                    envelope.eventType(),
                    envelope.aggregateType(),
                    envelope.aggregateId(),
                    envelope.payload()
            );
        }

        return new KafkaDomainEventMessage(
                UUID.fromString(requiredHeader(record, "event-id")),
                requiredHeader(record, "event-type"),
                requiredHeader(record, "aggregate-type"),
                requiredHeader(record, "aggregate-id"),
                record.value()
        );
    }

    private DomainEventEnvelope readEnvelope(String value) {
        try {
            return objectMapper.readValue(value, DomainEventEnvelope.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String requiredHeader(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        if (header == null) {
            throw new IllegalArgumentException("Kafka domain event is missing required header: " + name);
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    public record UserRegisteredPayload(Long userId, String username, String email, LocalDateTime occurredAt) {
    }
}
