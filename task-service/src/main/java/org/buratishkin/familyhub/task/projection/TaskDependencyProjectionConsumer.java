package org.buratishkin.familyhub.task.projection;

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
public class TaskDependencyProjectionConsumer {
    private static final String USER_REGISTERED = "org.buratishkin.familyhub.auth.user.api.event.UserRegisteredEvent";
    private static final String MEMBER_ADDED = "org.buratishkin.familyhub.family.member.api.event.MemberAddedEvent";
    private static final String MEMBER_REMOVED = "org.buratishkin.familyhub.family.member.api.event.MemberRemovedEvent";
    private static final String FAMILY_DELETED = "org.buratishkin.familyhub.family.api.event.FamilyDeletedEvent";
    private static final String ADDRESS_CREATED = "org.buratishkin.familyhub.address.api.event.AddressCreatedEvent";
    private static final String ADDRESS_UPDATED = "org.buratishkin.familyhub.address.api.event.AddressUpdatedEvent";
    private static final String ADDRESS_DELETED = "org.buratishkin.familyhub.address.api.event.AddressDeletedEvent";

    private final ProcessedEventService processedEventService;
    private final TaskDependencyProjectionService projectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${family-hub.outbox.topic:familyhub.domain-events}",
            groupId = "${family-hub.kafka.consumer.task-projections.group-id:task-service-task-projections}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        KafkaDomainEventMessage message = toMessage(record);
        processedEventService.processOnce(message, () -> apply(message));
    }

    private void apply(KafkaDomainEventMessage message) {
        try {
            switch (message.eventType()) {
                case USER_REGISTERED -> applyUserRegistered(message.payload());
                case MEMBER_ADDED -> applyMemberAdded(message.payload());
                case MEMBER_REMOVED -> applyMemberRemoved(message.payload());
                case FAMILY_DELETED -> applyFamilyDeleted(message.payload());
                case ADDRESS_CREATED -> applyAddressCreated(message.payload());
                case ADDRESS_UPDATED -> applyAddressUpdated(message.payload());
                case ADDRESS_DELETED -> applyAddressDeleted(message.payload());
                default -> log.debug("Task dependency projection ignored eventType={}", message.eventType());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to apply task dependency projection event " + message.eventType(), e);
        }
    }

    private void applyUserRegistered(String payload) throws Exception {
        UserRegisteredPayload event = objectMapper.readValue(payload, UserRegisteredPayload.class);
        projectionService.applyUserRegistered(event.userId(), event.username(), event.email());
    }

    private void applyMemberAdded(String payload) throws Exception {
        MemberChangedPayload event = objectMapper.readValue(payload, MemberChangedPayload.class);
        projectionService.applyMemberAdded(event.familyId(), event.memberId(), event.userId());
    }

    private void applyMemberRemoved(String payload) throws Exception {
        MemberChangedPayload event = objectMapper.readValue(payload, MemberChangedPayload.class);
        projectionService.applyMemberRemoved(event.memberId());
    }

    private void applyFamilyDeleted(String payload) throws Exception {
        FamilyDeletedPayload event = objectMapper.readValue(payload, FamilyDeletedPayload.class);
        projectionService.applyFamilyDeleted(event.familyId());
    }

    private void applyAddressCreated(String payload) throws Exception {
        AddressCreatedPayload event = objectMapper.readValue(payload, AddressCreatedPayload.class);
        projectionService.applyAddressChanged(event.addressId(), event.familyId(), event.categoryId());
    }

    private void applyAddressUpdated(String payload) throws Exception {
        AddressUpdatedPayload event = objectMapper.readValue(payload, AddressUpdatedPayload.class);
        projectionService.applyAddressChanged(event.addressId(), event.familyId(), event.categoryId());
    }

    private void applyAddressDeleted(String payload) throws Exception {
        AddressDeletedPayload event = objectMapper.readValue(payload, AddressDeletedPayload.class);
        projectionService.applyAddressDeleted(event.addressId());
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

    public record MemberChangedPayload(Long familyId, Long memberId, Long userId, LocalDateTime occurredAt) {
    }

    public record FamilyDeletedPayload(Long familyId, Long deletedByUserId, LocalDateTime occurredAt) {
    }

    public record AddressCreatedPayload(Long addressId, Long familyId, Long categoryId, Long createdByUserId, LocalDateTime occurredAt) {
    }

    public record AddressUpdatedPayload(Long addressId, Long familyId, Long categoryId, Long updatedByUserId, LocalDateTime occurredAt) {
    }

    public record AddressDeletedPayload(Long addressId, Long familyId, Long deletedByUserId, LocalDateTime occurredAt) {
    }
}
