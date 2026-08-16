package org.buratishkin.familyhub.notification.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.buratishkin.familyhub.notification.dto.CreateNotificationCommand;
import org.buratishkin.familyhub.notification.projection.NotificationProjectionService;
import org.buratishkin.familyhub.notification.service.NotificationService;
import org.buratishkin.familyhub.shared.event.DomainEventEnvelope;
import org.buratishkin.familyhub.shared.inbox.KafkaDomainEventMessage;
import org.buratishkin.familyhub.shared.inbox.ProcessedEventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDomainEventConsumer {
    private static final String POLL_NOTIFICATION_TYPE = "POLL_CREATED";
    private static final String POLL_AGGREGATE_TYPE = "Poll";
    private static final String PLAN_AGGREGATE_TYPE = "FamilyPlan";
    private static final String TASK_AGGREGATE_TYPE = "Task";
    private static final int MAX_TITLE_LENGTH = 255;

    private static final String USER_REGISTERED = "org.buratishkin.familyhub.auth.user.api.event.UserRegisteredEvent";
    private static final String FAMILY_CREATED = "org.buratishkin.familyhub.family.api.event.FamilyCreatedEvent";
    private static final String FAMILY_DELETED = "org.buratishkin.familyhub.family.api.event.FamilyDeletedEvent";
    private static final String FAMILY_ADMIN_CHANGED = "org.buratishkin.familyhub.family.api.event.FamilyAdminChangedEvent";
    private static final String MEMBER_ADDED = "org.buratishkin.familyhub.family.member.api.event.MemberAddedEvent";
    private static final String MEMBER_REMOVED = "org.buratishkin.familyhub.family.member.api.event.MemberRemovedEvent";
    private static final String INVITE_CREATED = "org.buratishkin.familyhub.family.invite.api.event.InviteCreatedEvent";
    private static final String INVITE_REDEEMED = "org.buratishkin.familyhub.family.invite.api.event.InviteRedeemedEvent";
    private static final String ALIAS_CREATED = "org.buratishkin.familyhub.family.alias.api.event.AliasCreatedEvent";
    private static final String ALIAS_UPDATED = "org.buratishkin.familyhub.family.alias.api.event.AliasUpdatedEvent";
    private static final String ALIAS_DELETED = "org.buratishkin.familyhub.family.alias.api.event.AliasDeletedEvent";
    private static final String POLL_CREATED = "org.buratishkin.familyhub.family.poll.api.event.PollCreatedEvent";
    private static final String FAMILY_PLAN_CREATED = "org.buratishkin.familyhub.family.plan.api.event.FamilyPlanCreatedEvent";
    private static final String FAMILY_PLAN_UPDATED = "org.buratishkin.familyhub.family.plan.api.event.FamilyPlanUpdatedEvent";
    private static final String FAMILY_PLAN_DELETED = "org.buratishkin.familyhub.family.plan.api.event.FamilyPlanDeletedEvent";
    private static final String TASK_CREATED = "org.buratishkin.familyhub.task.api.event.TaskCreatedEvent";
    private static final String TASK_ASSIGNED = "org.buratishkin.familyhub.task.api.event.TaskAssignedEvent";
    private static final String TASK_UPDATED = "org.buratishkin.familyhub.task.api.event.TaskUpdatedEvent";
    private static final String TASK_DELETED = "org.buratishkin.familyhub.task.api.event.TaskDeletedEvent";
    private static final String TASK_REMINDER_DUE = "org.buratishkin.familyhub.task.api.event.TaskReminderDueEvent";
    private static final String ADDRESS_CREATED = "org.buratishkin.familyhub.address.api.event.AddressCreatedEvent";
    private static final String ADDRESS_UPDATED = "org.buratishkin.familyhub.address.api.event.AddressUpdatedEvent";
    private static final String ADDRESS_DELETED = "org.buratishkin.familyhub.address.api.event.AddressDeletedEvent";
    private static final String CATEGORY_CREATED = "org.buratishkin.familyhub.address.category.api.event.CategoryCreatedEvent";
    private static final String CATEGORY_UPDATED = "org.buratishkin.familyhub.address.category.api.event.CategoryUpdatedEvent";
    private static final String CATEGORY_ARCHIVED = "org.buratishkin.familyhub.address.category.api.event.CategoryArchivedEvent";
    private static final String RECIPE_CREATED = "org.buratishkin.familyhub.meal.recipe.api.event.RecipeCreatedEvent";

    private final ProcessedEventService processedEventService;
    private final NotificationProjectionService projectionService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${family-hub.outbox.topic:familyhub.domain-events}",
            groupId = "${family-hub.kafka.consumer.notifications.group-id:notification-service-notifications}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        KafkaDomainEventMessage message = toMessage(record);
        processedEventService.processOnce(message, () -> apply(message));
    }

    private void apply(KafkaDomainEventMessage message) {
        try {
            switch (message.eventType()) {
                case USER_REGISTERED -> applyUserRegistered(message.payload());
                case FAMILY_CREATED -> notifyFamilyCreated(message);
                case FAMILY_DELETED -> notifyFamilyDeleted(message);
                case FAMILY_ADMIN_CHANGED -> notifyFamily(message, FamilyAdminChangedPayload.class,
                        "FAMILY_ADMIN_CHANGED", "Сменился администратор семьи", "В семье назначен новый администратор");
                case MEMBER_ADDED -> notifyMemberAdded(message);
                case MEMBER_REMOVED -> notifyMemberRemoved(message);
                case INVITE_CREATED -> notifyFamily(message, InviteCreatedPayload.class,
                        "INVITE_CREATED", "Создано приглашение", "Для семьи создано новое приглашение");
                case INVITE_REDEEMED -> notifyInviteRedeemed(message);
                case ALIAS_CREATED -> notifyFamily(message, AliasChangedPayload.class,
                        "ALIAS_CREATED", "Добавлен псевдоним", "В семье добавлен псевдоним участника");
                case ALIAS_UPDATED -> notifyFamily(message, AliasChangedPayload.class,
                        "ALIAS_UPDATED", "Обновлен псевдоним", "В семье обновлен псевдоним участника");
                case ALIAS_DELETED -> notifyFamily(message, AliasChangedPayload.class,
                        "ALIAS_DELETED", "Удален псевдоним", "В семье удален псевдоним участника");
                case POLL_CREATED -> notifyPollCreated(message);
                case FAMILY_PLAN_CREATED -> notifyFamilyPlanChanged(message, "PLAN_CREATED", "Добавлена занятость", "добавила занятость");
                case FAMILY_PLAN_UPDATED -> notifyFamilyPlanChanged(message, "PLAN_UPDATED", "Обновлена занятость", "обновила занятость");
                case FAMILY_PLAN_DELETED -> notifyFamilyPlanChanged(message, "PLAN_DELETED", "Удалена занятость", "удалила занятость");
                case TASK_CREATED -> notifyTaskCreated(message);
                case TASK_ASSIGNED -> notifyTaskAssigned(message);
                case TASK_UPDATED -> notifyTaskUpdated(message);
                case TASK_DELETED -> notifyTaskDeleted(message);
                case TASK_REMINDER_DUE -> notifyTaskReminderDue(message);
                case ADDRESS_CREATED -> notifyAddressCreated(message);
                case ADDRESS_UPDATED -> notifyAddressUpdated(message);
                case ADDRESS_DELETED -> notifyAddressDeleted(message);
                case CATEGORY_CREATED -> notifyCategoryCreated(message);
                case CATEGORY_UPDATED -> notifyCategoryUpdated(message);
                case CATEGORY_ARCHIVED -> notifyCategoryArchived(message);
                case RECIPE_CREATED -> notifyRecipeCreated(message);
                default -> log.debug("Notification service ignored eventType={}", message.eventType());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to process notification event " + message.eventType(), e);
        }
    }

    private void applyUserRegistered(String payload) throws Exception {
        UserRegisteredPayload event = objectMapper.readValue(payload, UserRegisteredPayload.class);
        projectionService.applyUserRegistered(event.userId(), event.username(), event.email());
    }

    private void notifyFamilyCreated(KafkaDomainEventMessage message) throws Exception {
        FamilyCreatedPayload event = objectMapper.readValue(message.payload(), FamilyCreatedPayload.class);
        Set<Long> recipients = new LinkedHashSet<>();
        recipients.add(event.adminUserId());
        notifyUsers(recipients, message, event.familyId(), "FAMILY_CREATED", "Семья создана", "Ваша семья создана");
    }

    private void notifyFamilyDeleted(KafkaDomainEventMessage message) throws Exception {
        FamilyDeletedPayload event = objectMapper.readValue(message.payload(), FamilyDeletedPayload.class);
        Set<Long> recipients = projectionService.findActiveUserIdsByFamilyId(event.familyId());
        recipients.add(event.deletedByUserId());
        notifyUsers(recipients, message, event.familyId(), "FAMILY_DELETED", "Семья удалена", "Семья была удалена");
        projectionService.applyFamilyDeleted(event.familyId());
    }

    private void notifyMemberAdded(KafkaDomainEventMessage message) throws Exception {
        MemberAddedPayload event = objectMapper.readValue(message.payload(), MemberAddedPayload.class);
        projectionService.applyMemberAdded(event.familyId(), event.memberId(), event.userId());
        notifyFamily(
                message,
                event.familyId(),
                "MEMBER_ADDED",
                "Новый участник семьи",
                "К семье добавлен новый участник"
        );
    }

    private void notifyMemberRemoved(KafkaDomainEventMessage message) throws Exception {
        MemberRemovedPayload event = objectMapper.readValue(message.payload(), MemberRemovedPayload.class);
        Set<Long> recipients = projectionService.findActiveUserIdsByFamilyId(event.familyId());
        recipients.add(event.userId());
        notifyUsers(recipients, message, event.familyId(), "MEMBER_REMOVED", "Участник удален", "Участник удален из семьи");
        projectionService.applyMemberRemoved(event.memberId());
    }

    private void notifyInviteRedeemed(KafkaDomainEventMessage message) throws Exception {
        InviteRedeemedPayload event = objectMapper.readValue(message.payload(), InviteRedeemedPayload.class);
        projectionService.applyMemberAdded(event.familyId(), event.memberId(), event.userId());
        notifyFamily(
                message,
                event.familyId(),
                "INVITE_REDEEMED",
                "Приглашение принято",
                "К семье присоединился новый участник"
        );
    }

    private void notifyTaskCreated(KafkaDomainEventMessage message) throws Exception {
        TaskCreatedPayload event = objectMapper.readValue(message.payload(), TaskCreatedPayload.class);
        if (event.assigneeMemberId() != null) {
            notifyMember(
                    event.assigneeMemberId(),
                    message,
                    event.familyId(),
                    "TASK_CREATED",
                    titleWithName("Вам назначена задача", event.taskName()),
                    bodyWithName("Вам назначена задача", event.taskName(), "Вам назначена новая семейная задача")
            );
            return;
        }
        notifyFamily(
                message,
                event.familyId(),
                "TASK_CREATED",
                titleWithName("Новая задача", event.taskName()),
                bodyWithName("В семье создана задача", event.taskName(), "В семье создана новая задача")
        );
    }

    private void notifyPollCreated(KafkaDomainEventMessage message) throws Exception {
        PollCreatedPayload event = objectMapper.readValue(message.payload(), PollCreatedPayload.class);
        Set<Long> recipients = projectionService.findActiveUserIdsByFamilyId(event.familyId());
        recipients.remove(event.actorUserId());
        notificationService.createForUsers(
                recipients,
                new CreateNotificationCommand(
                        event.familyId(),
                        POLL_NOTIFICATION_TYPE,
                        pollTitle(event.question()),
                        pollBody(event.actorName()),
                        message.eventId(),
                        message.eventType(),
                        POLL_AGGREGATE_TYPE,
                        event.pollId() == null ? message.aggregateId() : event.pollId().toString()
                )
        );
    }

    private void notifyFamilyPlanChanged(KafkaDomainEventMessage message,
                                         String type,
                                         String titlePrefix,
                                         String bodyAction) throws Exception {
        FamilyPlanChangedPayload event = objectMapper.readValue(message.payload(), FamilyPlanChangedPayload.class);
        Set<Long> recipients = projectionService.findActiveUserIdsByFamilyId(event.familyId());
        recipients.remove(event.actorUserId());
        notificationService.createForUsers(
                recipients,
                new CreateNotificationCommand(
                        event.familyId(),
                        type,
                        titleWithName(titlePrefix, event.title()),
                        planBody(event.actorName(), bodyAction, event.title()),
                        message.eventId(),
                        message.eventType(),
                        PLAN_AGGREGATE_TYPE,
                        event.familyPlanId() == null ? message.aggregateId() : event.familyPlanId().toString()
                )
        );
    }

    private void notifyTaskAssigned(KafkaDomainEventMessage message) throws Exception {
        TaskAssignedPayload event = objectMapper.readValue(message.payload(), TaskAssignedPayload.class);
        if (event.previousAssigneeMemberId() == null) {
            return;
        }
        notifyMember(
                event.newAssigneeMemberId(),
                message,
                event.familyId(),
                "TASK_ASSIGNED",
                titleWithName("Задача назначена", event.taskName()),
                bodyWithName("Вам назначена задача", event.taskName(), "Вам назначена семейная задача")
        );
    }

    private void notifyTaskUpdated(KafkaDomainEventMessage message) throws Exception {
        TaskUpdatedPayload event = objectMapper.readValue(message.payload(), TaskUpdatedPayload.class);
        notifyFamily(
                message,
                event.familyId(),
                "TASK_UPDATED",
                titleWithName("Задача обновлена", event.taskName()),
                bodyWithName("Обновлена задача", event.taskName(), "В семейной задаче появились изменения")
        );
    }

    private void notifyTaskDeleted(KafkaDomainEventMessage message) throws Exception {
        TaskDeletedPayload event = objectMapper.readValue(message.payload(), TaskDeletedPayload.class);
        notifyFamily(
                message,
                event.familyId(),
                "TASK_DELETED",
                titleWithName("Задача удалена", event.taskName()),
                bodyWithName("Удалена задача", event.taskName(), "Семейная задача была удалена")
        );
    }

    private void notifyTaskReminderDue(KafkaDomainEventMessage message) throws Exception {
        TaskReminderDuePayload event = objectMapper.readValue(message.payload(), TaskReminderDuePayload.class);
        Long recipientMemberId = event.assigneeMemberId() == null ? event.creatorMemberId() : event.assigneeMemberId();
        notifyMember(
                recipientMemberId,
                message,
                event.familyId(),
                "TASK_REMINDER_" + event.reminderMinutes() + "M",
                taskReminderTitle(event.reminderMinutes(), event.taskName()),
                taskReminderBody(event.reminderMinutes(), event.taskName()),
                TASK_AGGREGATE_TYPE,
                event.taskId() == null ? message.aggregateId() : event.taskId().toString()
        );
    }

    private void notifyAddressCreated(KafkaDomainEventMessage message) throws Exception {
        AddressCreatedPayload event = objectMapper.readValue(message.payload(), AddressCreatedPayload.class);
        notifyFamily(
                message,
                event.familyId(),
                "ADDRESS_CREATED",
                titleWithName("Добавлен адрес", event.addressName()),
                bodyWithName("В семье добавлен адрес", event.addressName(), "В семье добавлен новый адрес")
        );
    }

    private void notifyAddressUpdated(KafkaDomainEventMessage message) throws Exception {
        AddressUpdatedPayload event = objectMapper.readValue(message.payload(), AddressUpdatedPayload.class);
        notifyFamily(
                message,
                event.familyId(),
                "ADDRESS_UPDATED",
                titleWithName("Адрес обновлен", event.addressName()),
                bodyWithName("Обновлен адрес", event.addressName(), "Семейный адрес был обновлен")
        );
    }

    private void notifyAddressDeleted(KafkaDomainEventMessage message) throws Exception {
        AddressDeletedPayload event = objectMapper.readValue(message.payload(), AddressDeletedPayload.class);
        notifyFamily(
                message,
                event.familyId(),
                "ADDRESS_DELETED",
                titleWithName("Адрес удален", event.addressName()),
                bodyWithName("Удален адрес", event.addressName(), "Семейный адрес был удален")
        );
    }

    private void notifyCategoryCreated(KafkaDomainEventMessage message) throws Exception {
        CategoryCreatedPayload event = objectMapper.readValue(message.payload(), CategoryCreatedPayload.class);
        notifyFamily(
                message,
                event.familyId(),
                "CATEGORY_CREATED",
                titleWithName("Добавлена категория", event.categoryName()),
                bodyWithName("В семье добавлена категория", event.categoryName(), "В семье добавлена новая категория адресов")
        );
    }

    private void notifyCategoryUpdated(KafkaDomainEventMessage message) throws Exception {
        CategoryUpdatedPayload event = objectMapper.readValue(message.payload(), CategoryUpdatedPayload.class);
        notifyFamily(
                message,
                event.familyId(),
                "CATEGORY_UPDATED",
                titleWithName("Категория обновлена", event.categoryName()),
                bodyWithName("Обновлена категория", event.categoryName(), "Семейная категория адресов была обновлена")
        );
    }

    private void notifyCategoryArchived(KafkaDomainEventMessage message) throws Exception {
        CategoryArchivedPayload event = objectMapper.readValue(message.payload(), CategoryArchivedPayload.class);
        notifyFamily(
                message,
                event.familyId(),
                "CATEGORY_ARCHIVED",
                titleWithName("Категория архивирована", event.categoryName()),
                bodyWithName("Архивирована категория", event.categoryName(), "Семейная категория адресов была архивирована")
        );
    }

    private void notifyRecipeCreated(KafkaDomainEventMessage message) throws Exception {
        RecipeCreatedPayload event = objectMapper.readValue(message.payload(), RecipeCreatedPayload.class);
        notifyFamily(
                message,
                event.familyId(),
                "FOOD_RECIPE_CREATED",
                titleWithName("Добавлено блюдо", event.recipeName()),
                bodyWithName("В семейную книгу рецептов добавлено блюдо", event.recipeName(), "В семейную книгу рецептов добавлено новое блюдо")
        );
    }

    private <T extends FamilyPayload> void notifyFamily(KafkaDomainEventMessage message,
                                                       Class<T> payloadType,
                                                       String type,
                                                       String title,
                                                       String body) throws Exception {
        T payload = objectMapper.readValue(message.payload(), payloadType);
        notifyFamily(message, payload.familyId(), type, title, body);
    }

    private void notifyFamily(KafkaDomainEventMessage message,
                              Long familyId,
                              String type,
                              String title,
                              String body) {
        notifyUsers(projectionService.findActiveUserIdsByFamilyId(familyId), message, familyId, type, title, body);
    }

    private void notifyMember(Long memberId,
                              KafkaDomainEventMessage message,
                              Long familyId,
                              String type,
                              String title,
                              String body) {
        notifyMember(memberId, message, familyId, type, title, body, message.aggregateType(), message.aggregateId());
    }

    private void notifyMember(Long memberId,
                              KafkaDomainEventMessage message,
                              Long familyId,
                              String type,
                              String title,
                              String body,
                              String aggregateType,
                              String aggregateId) {
        Set<Long> recipients = new LinkedHashSet<>();
        projectionService.findUserIdByMemberId(memberId).ifPresent(recipients::add);
        notificationService.createForUsers(recipients, new CreateNotificationCommand(
                familyId,
                type,
                title,
                body,
                message.eventId(),
                message.eventType(),
                aggregateType,
                aggregateId
        ));
    }

    private void notifyUsers(Set<Long> recipients,
                             KafkaDomainEventMessage message,
                             Long familyId,
                             String type,
                             String title,
                             String body) {
        notificationService.createForUsers(recipients, new CreateNotificationCommand(
                familyId,
                type,
                title,
                body,
                message.eventId(),
                message.eventType(),
                message.aggregateType(),
                message.aggregateId()
        ));
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

    private interface FamilyPayload {
        Long familyId();
    }

    public record UserRegisteredPayload(Long userId, String username, String email, LocalDateTime occurredAt) {
    }

    public record FamilyCreatedPayload(Long familyId, Long adminMemberId, Long adminUserId, LocalDateTime occurredAt) {
    }

    public record FamilyDeletedPayload(Long familyId, Long deletedByUserId, LocalDateTime occurredAt) implements FamilyPayload {
    }

    public record FamilyAdminChangedPayload(
            Long familyId,
            Long previousAdminMemberId,
            Long newAdminMemberId,
            LocalDateTime occurredAt
    ) implements FamilyPayload {
    }

    public record MemberAddedPayload(Long familyId, Long memberId, Long userId, LocalDateTime occurredAt) implements FamilyPayload {
    }

    public record MemberRemovedPayload(Long familyId, Long memberId, Long userId, LocalDateTime occurredAt) implements FamilyPayload {
    }

    public record InviteCreatedPayload(
            Long inviteId,
            Long familyId,
            Long createdByMemberId,
            LocalDateTime expiresAt,
            LocalDateTime occurredAt
    ) implements FamilyPayload {
    }

    public record InviteRedeemedPayload(Long inviteId, Long familyId, Long memberId, Long userId, LocalDateTime occurredAt) implements FamilyPayload {
    }

    public record AliasChangedPayload(
            Long aliasId,
            Long familyId,
            Long ownerMemberId,
            Long targetMemberId,
            LocalDateTime occurredAt
    ) implements FamilyPayload {
    }

    public record PollCreatedPayload(
            Long pollId,
            Long familyId,
            Long creatorMemberId,
            Long actorUserId,
            String question,
            String actorName,
            LocalDateTime occurredAt
    ) implements FamilyPayload {
    }

    public record FamilyPlanChangedPayload(
            Long familyPlanId,
            Long familyId,
            Long memberId,
            Long actorUserId,
            String title,
            String actorName,
            LocalDateTime busyFrom,
            LocalDateTime busyTo,
            LocalDateTime occurredAt
    ) implements FamilyPayload {
    }

    public record TaskCreatedPayload(
            Long taskId,
            Long familyId,
            Long creatorMemberId,
            Long assigneeMemberId,
            Long addressId,
            String taskName,
            LocalDateTime occurredAt
    ) implements FamilyPayload {
    }

    public record TaskAssignedPayload(
            Long taskId,
            Long familyId,
            Long previousAssigneeMemberId,
            Long newAssigneeMemberId,
            String taskName,
            LocalDateTime occurredAt
    ) implements FamilyPayload {
    }

    public record TaskUpdatedPayload(Long taskId, Long familyId, Long updatedByUserId, String taskName, LocalDateTime occurredAt) implements FamilyPayload {
    }

    public record TaskDeletedPayload(Long taskId, Long familyId, Long deletedByUserId, String taskName, LocalDateTime occurredAt) implements FamilyPayload {
    }

    public record TaskReminderDuePayload(
            Long taskReminderId,
            Long taskId,
            Long familyId,
            Long creatorMemberId,
            Long assigneeMemberId,
            String taskName,
            LocalDateTime taskStart,
            Integer reminderMinutes,
            LocalDateTime occurredAt
    ) implements FamilyPayload {
    }

    public record AddressCreatedPayload(
            Long addressId,
            Long familyId,
            Long categoryId,
            Long createdByUserId,
            String addressName,
            LocalDateTime occurredAt
    ) implements FamilyPayload {
    }

    public record AddressUpdatedPayload(
            Long addressId,
            Long familyId,
            Long categoryId,
            Long updatedByUserId,
            String addressName,
            LocalDateTime occurredAt
    ) implements FamilyPayload {
    }

    public record AddressDeletedPayload(Long addressId, Long familyId, Long deletedByUserId, String addressName, LocalDateTime occurredAt) implements FamilyPayload {
    }

    public record CategoryCreatedPayload(Long categoryId, Long familyId, Long createdByUserId, String categoryName, LocalDateTime occurredAt) implements FamilyPayload {
    }

    public record CategoryUpdatedPayload(Long categoryId, Long familyId, Long updatedByUserId, String categoryName, LocalDateTime occurredAt) implements FamilyPayload {
    }

    public record CategoryArchivedPayload(Long categoryId, Long familyId, Long archivedByUserId, String categoryName, LocalDateTime occurredAt) implements FamilyPayload {
    }

    public record RecipeCreatedPayload(Long recipeId, Long familyId, Long createdByUserId, String recipeName, LocalDateTime occurredAt) implements FamilyPayload {
    }

    private String titleWithName(String baseTitle, String name) {
        String normalizedName = normalizedName(name);
        if (normalizedName == null) {
            return baseTitle;
        }
        String title = baseTitle + ": " + normalizedName;
        return title.length() <= MAX_TITLE_LENGTH ? title : title.substring(0, MAX_TITLE_LENGTH);
    }

    private String bodyWithName(String prefix, String name, String fallback) {
        String normalizedName = normalizedName(name);
        if (normalizedName == null) {
            return fallback;
        }
        return trimBody(prefix + " \"" + normalizedName + "\"");
    }

    private String normalizedName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String trimBody(String body) {
        if (body == null || body.isBlank()) {
            return "В семье создан новый опрос";
        }
        return body.length() <= 1000 ? body : body.substring(0, 1000);
    }

    private String pollTitle(String question) {
        String baseTitle = "Новый опрос";
        if (question == null || question.isBlank()) {
            return baseTitle;
        }

        String title = baseTitle + ": " + question.trim();
        return title.length() <= MAX_TITLE_LENGTH ? title : title.substring(0, MAX_TITLE_LENGTH);
    }

    private String pollBody(String actorName) {
        if (actorName == null || actorName.isBlank()) {
            return "Создан новый опрос";
        }
        return trimBody(actorName.trim() + " создала опрос");
    }

    private String planBody(String actorName, String action, String title) {
        String actor = actorName == null || actorName.isBlank() ? "Участник семьи" : actorName.trim();
        String normalizedTitle = normalizedName(title);
        if (normalizedTitle == null) {
            return trimBody(actor + " " + action);
        }
        return trimBody(actor + " " + action + ": \"" + normalizedTitle + "\"");
    }

    private String taskReminderTitle(Integer reminderMinutes, String taskName) {
        String prefix = reminderMinutes == null
                ? "Скоро задача"
                : "Задача через " + reminderMinutes + " мин";
        return titleWithName(prefix, taskName);
    }

    private String taskReminderBody(Integer reminderMinutes, String taskName) {
        String normalizedTaskName = normalizedName(taskName);
        String timeText = reminderMinutes == null ? "скоро" : "через " + reminderMinutes + " мин";
        if (normalizedTaskName == null) {
            return trimBody("Задача начнется " + timeText);
        }
        return trimBody("Задача \"" + normalizedTaskName + "\" начнется " + timeText);
    }
}
