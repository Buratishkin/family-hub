package org.buratishkin.familyhub.controller.me;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.address.api.AddressLookupApi;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.alias.api.AliasView;
import org.buratishkin.familyhub.family.alias.api.AliasLookupApi;
import org.buratishkin.familyhub.address.category.api.CategoryLookupApi;
import org.buratishkin.familyhub.family.api.FamilyAccessApi;
import org.buratishkin.familyhub.family.api.FamilyLookupApi;
import org.buratishkin.familyhub.family.api.FamilyView;
import org.buratishkin.familyhub.family.member.api.MemberLookupApi;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.integration.task.TaskServiceClient;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/me")
public class MeController {
    private final FamilyAccessApi familyAccessService;
    private final AddressLookupApi addressCrudService;
    private final AliasLookupApi aliasCrudService;
    private final CategoryLookupApi categoryCrudService;
    private final TaskServiceClient taskServiceClient;
    private final MemberLookupApi memberLookupApi;
    private final FamilyLookupApi familyLookupApi;

    @GetMapping("/bootstrap")
    public BootstrapResp bootstrap(Authentication authentication,
                                   @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader){
        UserView user = familyAccessService.currentUser(authentication.getName());

        List<MemberView> members = memberLookupApi.findMembersByUserId(user.id());
        List<BootstrapResp.FamilySummary> families = members.stream()
                .map(member -> toFamilySummary(member, authorizationHeader))
                .toList();

        return new BootstrapResp(
                user.id(),
                user.username(),
                user.email(),
                families,
                Instant.now()
        );
    }

    @GetMapping("/families/{familyId}/overview")
    public FamilyOverviewResp getFamilyOverview(@PathVariable Long familyId,
                                                Authentication authentication,
                                                @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        UserView user = familyAccessService.currentUser(authentication.getName());
        if (!familyAccessService.hasFamilyAccess(user.id(), familyId)) {
            return invalidFamilyOverviewResp();
        }

        MemberView currentMember = memberLookupApi.findMemberByFamilyIdAndUserId(familyId, user.id());
        if (currentMember == null) {
            return invalidFamilyOverviewResp();
        }

        return new FamilyOverviewResp(
                true,
                "ok",
                toFamilySummary(currentMember, authorizationHeader),
                getCategorySummaries(familyId),
                getAddressSummaries(familyId),
                getTaskSummaries(familyId, authorizationHeader),
                getAliasSummaries(familyId),
                Instant.now()
        );
    }

    private BootstrapResp.FamilySummary toFamilySummary(MemberView member, String authorizationHeader) {
        FamilyView family = familyLookupApi.findFamilyById(member.familyId());
        List<MemberView> familyMembers = memberLookupApi.findMembersByFamilyId(family.id());
        List<AliasView> aliases = aliasCrudService.findAliasesByFamilyId(family.id());

        int tasksToDoCount = taskServiceClient.countByAssigneeMemberId(member.id(), authorizationHeader);
        int tasksCreatedByMeCount = taskServiceClient.countByCreatorMemberId(member.id(), authorizationHeader);
        int memberCount = familyMembers.size();
        int userCount = (int) familyMembers.stream()
                .filter(familyMember -> familyMember.userId() != null)
                .count();
        List<BootstrapResp.MemberSummary> members = familyMembers.stream()
                .map(familyMember -> new BootstrapResp.MemberSummary(
                        familyMember.id(),
                        resolveMemberName(member, familyMember, aliases)
                ))
                .toList();
        List<BootstrapResp.AliasSummary> myAliases = aliases.stream()
                .filter(alias -> Objects.equals(alias.ownerMemberId(), member.id()))
                .map(alias -> new BootstrapResp.AliasSummary(
                        alias.id(),
                        alias.targetMemberId(),
                        alias.alias()
                ))
                .toList();

        return new BootstrapResp.FamilySummary(
                family.id(),
                family.name(),
                family.description(),
                member.role() == null ? null : member.role().name(),
                members,
                memberCount,
                userCount,
                tasksToDoCount,
                tasksCreatedByMeCount,
                myAliases
        );
    }

    private String resolveMemberName(MemberView owner, MemberView target, List<AliasView> aliases) {
        for (AliasView alias : aliases) {
            if (Objects.equals(alias.ownerMemberId(), owner.id())
                    && Objects.equals(alias.targetMemberId(), target.id())) {
                return alias.alias();
            }
        }
        if (target.defaultName() != null && !target.defaultName().isBlank()) {
            return target.defaultName();
        }
        if (target.username() != null && !target.username().isBlank()) {
            return target.username();
        }
        return "member-" + target.id();
    }

    @GetMapping("/families/{familyId}/addresses")
    public FamilyItemsResp<AddressSummary> getAddresses(@PathVariable Long familyId, Authentication authentication){
        UserView user = familyAccessService.currentUser(authentication.getName());
        if (!familyAccessService.hasFamilyAccess(user.id(), familyId)) {
            return new FamilyItemsResp<>(false, "invalid familyId", List.of());
        }

        List<AddressSummary> items = getAddressSummaries(familyId);
        return new FamilyItemsResp<>(true, "ok", items);
    }

    @GetMapping("/families/{familyId}/categories")
    public FamilyItemsResp<CategorySummary> getCategories(@PathVariable Long familyId, Authentication authentication){
        UserView user = familyAccessService.currentUser(authentication.getName());
        if (!familyAccessService.hasFamilyAccess(user.id(), familyId)) {
            return new FamilyItemsResp<>(false, "invalid familyId", List.of());
        }

        List<CategorySummary> items = getCategorySummaries(familyId);
        return new FamilyItemsResp<>(true, "ok", items);
    }

    @GetMapping("/families/{familyId}/tasks")
    public FamilyItemsResp<TaskSummary> getTasks(@PathVariable Long familyId,
                                                 Authentication authentication,
                                                 @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader){
        UserView user = familyAccessService.currentUser(authentication.getName());
        if (!familyAccessService.hasFamilyAccess(user.id(), familyId)) {
            return new FamilyItemsResp<>(false, "invalid familyId", List.of());
        }

        List<TaskSummary> items = getTaskSummaries(familyId, authorizationHeader);

        return new FamilyItemsResp<>(true, "ok", items);
    }

    @GetMapping("/families/{familyId}/tasks/old")
    public FamilyItemsResp<TaskSummary> getOldTasks(@PathVariable Long familyId,
                                                    Authentication authentication,
                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader){
        UserView user = familyAccessService.currentUser(authentication.getName());
        if (!familyAccessService.hasFamilyAccess(user.id(), familyId)) {
            return new FamilyItemsResp<>(false, "invalid familyId", List.of());
        }

        List<TaskSummary> items = getOldTaskSummaries(familyId, authorizationHeader);

        return new FamilyItemsResp<>(true, "ok", items);
    }

    @GetMapping("/families/{familyId}/aliases")
    public FamilyItemsResp<AliasSummary> getAliases(@PathVariable Long familyId, Authentication authentication){
        UserView user = familyAccessService.currentUser(authentication.getName());
        if (!familyAccessService.hasFamilyAccess(user.id(), familyId)) {
            return new FamilyItemsResp<>(false, "invalid familyId", List.of());
        }

        List<AliasSummary> items = getAliasSummaries(familyId);

        return new FamilyItemsResp<>(true, "ok", items);
    }

    private List<AddressSummary> getAddressSummaries(Long familyId) {
        return addressCrudService.findAddressesByFamilyId(familyId).stream()
                .map(address -> new AddressSummary(
                        address.id(),
                        address.name(),
                        address.country(),
                        address.city(),
                        address.streetType(),
                        address.street(),
                        address.house(),
                        address.apartment(),
                        address.comment(),
                        address.categoryId()
                ))
                .toList();
    }

    private List<CategorySummary> getCategorySummaries(Long familyId) {
        return categoryCrudService.findCategoriesByFamilyId(familyId).stream()
                .map(category -> new CategorySummary(
                        category.id(),
                        category.name(),
                        category.type(),
                        category.archived(),
                        category.createdAt()
                ))
                .toList();
    }

    private List<TaskSummary> getTaskSummaries(Long familyId, String authorizationHeader) {
        return taskServiceClient.findTasksByFamilyId(familyId, authorizationHeader).stream()
                .map(task -> new TaskSummary(
                        task.id(),
                        task.name(),
                        task.description(),
                        task.start(),
                        task.type(),
                        task.status(),
                        task.creatorMemberId(),
                        task.assigneeMemberId(),
                        task.addressId(),
                        task.recurrenceSeriesId(),
                        task.recurrenceRootTaskId(),
                        task.recurrenceParentTaskId(),
                        task.recurrenceIndex(),
                        task.recurrence() == null ? null : new RecurrenceSummary(
                                task.recurrence().status(),
                                task.recurrence().frequency(),
                                task.recurrence().interval(),
                                task.recurrence().repeatUntil(),
                                task.recurrence().maxOccurrences()
                        )
                ))
                .toList();
    }

    private List<TaskSummary> getOldTaskSummaries(Long familyId, String authorizationHeader) {
        return taskServiceClient.findOldTasksByFamilyId(familyId, authorizationHeader).stream()
                .map(task -> new TaskSummary(
                        task.id(),
                        task.name(),
                        task.description(),
                        task.start(),
                        task.type(),
                        task.status(),
                        task.creatorMemberId(),
                        task.assigneeMemberId(),
                        task.addressId(),
                        task.recurrenceSeriesId(),
                        task.recurrenceRootTaskId(),
                        task.recurrenceParentTaskId(),
                        task.recurrenceIndex(),
                        task.recurrence() == null ? null : new RecurrenceSummary(
                                task.recurrence().status(),
                                task.recurrence().frequency(),
                                task.recurrence().interval(),
                                task.recurrence().repeatUntil(),
                                task.recurrence().maxOccurrences()
                        )
                ))
                .toList();
    }

    private List<AliasSummary> getAliasSummaries(Long familyId) {
        return aliasCrudService.findAliasesByFamilyId(familyId).stream()
                .map(alias -> new AliasSummary(
                        alias.id(),
                        alias.alias(),
                        alias.ownerMemberId(),
                        alias.targetMemberId()
                ))
                .toList();
    }

    private FamilyOverviewResp invalidFamilyOverviewResp() {
        return new FamilyOverviewResp(
                false,
                "invalid familyId",
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Instant.now()
        );
    }

    public record FamilyItemsResp<T>(boolean result, String reason, List<T> items) {}

    public record FamilyOverviewResp(
            boolean result,
            String reason,
            BootstrapResp.FamilySummary family,
            List<CategorySummary> categories,
            List<AddressSummary> addresses,
            List<TaskSummary> tasks,
            List<AliasSummary> aliases,
            Instant serverTime
    ) {}

    public record AddressSummary(
            Long id,
            String name,
            String country,
            String city,
            String streetType,
            String street,
            String house,
            String apartment,
            String comment,
            Long categoryId
    ) {}

    public record CategorySummary(
            Long id,
            String name,
            String type,
            boolean archived,
            Instant createdAt
    ) {}

    public record TaskSummary(
            Long id,
            String name,
            String description,
            java.time.LocalDateTime start,
            String type,
            String status,
            Long creatorId,
            Long assigneeId,
            Long addressId,
            Long recurrenceSeriesId,
            Long recurrenceRootTaskId,
            Long recurrenceParentTaskId,
            Integer recurrenceIndex,
            RecurrenceSummary recurrence
    ) {}

    public record RecurrenceSummary(
            String status,
            String frequency,
            Integer interval,
            java.time.LocalDateTime repeatUntil,
            Integer maxOccurrences
    ) {}

    public record AliasSummary(
            Long id,
            String alias,
            Long ownerId,
            Long targetId
    ) {}
}
