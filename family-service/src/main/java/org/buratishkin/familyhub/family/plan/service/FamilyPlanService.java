package org.buratishkin.familyhub.family.plan.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buratishkin.familyhub.auth.user.api.UserLookupApi;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.MemberRepository;
import org.buratishkin.familyhub.family.member.service.MemberCrudService;
import org.buratishkin.familyhub.family.plan.FamilyPlanEntity;
import org.buratishkin.familyhub.family.plan.FamilyPlanRepository;
import org.buratishkin.familyhub.family.plan.api.event.FamilyPlanCreatedEvent;
import org.buratishkin.familyhub.family.plan.api.event.FamilyPlanDeletedEvent;
import org.buratishkin.familyhub.family.plan.api.event.FamilyPlanUpdatedEvent;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanCreateReq;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanCreateResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanCreateResultResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanDeleteResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanListResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanResultResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanUpdateReq;
import org.buratishkin.familyhub.family.service.FamilyAccessService;
import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FamilyPlanService {
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final long MAX_QUERY_RANGE_DAYS = 370;
    private static final long DEFAULT_QUERY_RANGE_DAYS = 7;

    private final FamilyPlanRepository planRepository;
    private final MemberRepository memberRepository;
    private final MemberCrudService memberCrudService;
    private final FamilyAccessService familyAccessService;
    private final UserLookupApi userLookupApi;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(readOnly = true)
    public FamilyPlanListResp list(Long familyId,
                                   LocalDateTime from,
                                   LocalDateTime to,
                                   Long memberId,
                                   Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return listFalse("family not found or access denied");
        }

        Range range = normalizeRange(from, to);
        String rangeError = validateRange(range.from(), range.to());
        if (rangeError != null) {
            return listFalse(rangeError);
        }

        if (memberId != null && !memberCrudService.existsByIdAndFamilyId(memberId, familyId)) {
            return listFalse("member not found");
        }

        List<FamilyPlanEntity> plans = memberId == null
                ? planRepository.findAllByFamilyIdAndBusyFromLessThanAndBusyToGreaterThanOrderByBusyFromAscIdAsc(
                familyId,
                range.to(),
                range.from()
        )
                : planRepository.findAllByFamilyIdAndMemberIdAndBusyFromLessThanAndBusyToGreaterThanOrderByBusyFromAscIdAsc(
                familyId,
                memberId,
                range.to(),
                range.from()
        );

        return listTrue(toRespList(plans));
    }

    @Transactional(readOnly = true)
    public FamilyPlanResultResp find(Long familyId, Long planId, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return planFalse("family not found or access denied");
        }

        FamilyPlanEntity plan = planRepository.findByIdAndFamilyId(planId, familyId).orElse(null);
        if (plan == null) {
            return planFalse("plan not found");
        }

        return planTrue(toResp(plan));
    }

    @Transactional(readOnly = true)
    public List<FamilyPlanResp> findInternalConflicts(Long familyId, Long memberId, LocalDateTime start) {
        if (familyId == null || memberId == null || start == null) {
            return List.of();
        }
        return toRespList(planRepository
                .findAllByFamilyIdAndMemberIdAndBusyFromLessThanEqualAndBusyToGreaterThanOrderByBusyFromAscIdAsc(
                        familyId,
                        memberId,
                        start,
                        start
                ));
    }

    @Transactional
    public FamilyPlanCreateResultResp create(Long familyId,
                                             FamilyPlanCreateReq req,
                                             Authentication authentication) {
        String title = normalize(req == null ? null : req.title());
        String description = normalize(req == null ? null : req.description());
        LocalDateTime busyFrom = req == null ? null : req.busyFrom();
        LocalDateTime busyTo = req == null ? null : req.busyTo();

        String inputError = validateInput(title, description, busyFrom, busyTo);
        if (inputError != null) {
            return createFalse(inputError);
        }

        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return createFalse("family not found or access denied");
        }

        MemberEntity member = memberCrudService.findByFamilyIdAndUserId(familyId, user.id());
        LocalDateTime now = LocalDateTime.now();

        FamilyPlanEntity plan = new FamilyPlanEntity();
        plan.setFamilyId(familyId);
        plan.setMemberId(member.getId());
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setBusyFrom(busyFrom);
        plan.setBusyTo(busyTo);
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);

        FamilyPlanEntity saved = planRepository.save(plan);
        safePublish(new FamilyPlanCreatedEvent(
                saved.getId(),
                familyId,
                member.getId(),
                user.id(),
                saved.getTitle(),
                displayName(member, user),
                saved.getBusyFrom(),
                saved.getBusyTo(),
                now
        ));
        return new FamilyPlanCreateResultResp(
                true,
                "",
                new FamilyPlanCreateResp(saved.getId()),
                toResp(saved),
                LocalDateTime.now()
        );
    }

    @Transactional
    public FamilyPlanResultResp update(Long familyId,
                                       Long planId,
                                       FamilyPlanUpdateReq req,
                                       Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return planFalse("family not found or access denied");
        }

        FamilyPlanEntity plan = planRepository.findByIdAndFamilyId(planId, familyId).orElse(null);
        if (plan == null) {
            return planFalse("plan not found");
        }

        MemberEntity member = memberCrudService.findByFamilyIdAndUserId(familyId, user.id());
        if (!plan.getMemberId().equals(member.getId())) {
            return planFalse("only owner can change plan");
        }

        String title = req == null || req.title() == null ? plan.getTitle() : normalize(req.title());
        String description = req == null || req.description() == null ? plan.getDescription() : normalize(req.description());
        LocalDateTime busyFrom = req == null || req.busyFrom() == null ? plan.getBusyFrom() : req.busyFrom();
        LocalDateTime busyTo = req == null || req.busyTo() == null ? plan.getBusyTo() : req.busyTo();

        String inputError = validateInput(title, description, busyFrom, busyTo);
        if (inputError != null) {
            return planFalse(inputError);
        }

        plan.setTitle(title);
        plan.setDescription(description);
        plan.setBusyFrom(busyFrom);
        plan.setBusyTo(busyTo);
        plan.setUpdatedAt(LocalDateTime.now());

        FamilyPlanEntity saved = planRepository.save(plan);
        safePublish(new FamilyPlanUpdatedEvent(
                saved.getId(),
                familyId,
                member.getId(),
                user.id(),
                saved.getTitle(),
                displayName(member, user),
                saved.getBusyFrom(),
                saved.getBusyTo(),
                LocalDateTime.now()
        ));
        return planTrue(toResp(saved));
    }

    @Transactional
    public FamilyPlanDeleteResp delete(Long familyId, Long planId, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return deleteFalse("family not found or access denied");
        }

        FamilyPlanEntity plan = planRepository.findByIdAndFamilyId(planId, familyId).orElse(null);
        if (plan == null) {
            return deleteFalse("plan not found");
        }

        MemberEntity member = memberCrudService.findByFamilyIdAndUserId(familyId, user.id());
        if (!plan.getMemberId().equals(member.getId())) {
            return deleteFalse("only owner can change plan");
        }

        FamilyPlanDeletedEvent event = new FamilyPlanDeletedEvent(
                plan.getId(),
                familyId,
                member.getId(),
                user.id(),
                plan.getTitle(),
                displayName(member, user),
                plan.getBusyFrom(),
                plan.getBusyTo(),
                LocalDateTime.now()
        );
        planRepository.delete(plan);
        safePublish(event);
        return new FamilyPlanDeleteResp(true, "", LocalDateTime.now());
    }

    private List<FamilyPlanResp> toRespList(List<FamilyPlanEntity> plans) {
        Map<Long, String> names = memberNames(plans.stream()
                .map(FamilyPlanEntity::getMemberId)
                .collect(LinkedHashSet::new, Set::add, Set::addAll));
        return plans.stream()
                .map(plan -> toResp(plan, names))
                .toList();
    }

    private FamilyPlanResp toResp(FamilyPlanEntity plan) {
        return toResp(plan, memberNames(Set.of(plan.getMemberId())));
    }

    private FamilyPlanResp toResp(FamilyPlanEntity plan, Map<Long, String> memberNames) {
        return new FamilyPlanResp(
                plan.getId(),
                plan.getFamilyId(),
                plan.getMemberId(),
                memberNames.get(plan.getMemberId()),
                plan.getTitle(),
                plan.getDescription(),
                plan.getBusyFrom(),
                plan.getBusyTo(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }

    private Map<Long, String> memberNames(Set<Long> memberIds) {
        Map<Long, String> names = new HashMap<>();
        if (memberIds.isEmpty()) {
            return names;
        }
        for (MemberEntity member : memberRepository.findAllById(memberIds)) {
            names.put(member.getId(), displayName(member));
        }
        return names;
    }

    private String displayName(MemberEntity member) {
        if (member.getDefaultName() != null && !member.getDefaultName().isBlank()) {
            return member.getDefaultName();
        }
        if (member.getUserId() != null) {
            try {
                UserView user = userLookupApi.findUserById(member.getUserId());
                if (user != null && user.username() != null && !user.username().isBlank()) {
                    return user.username();
                }
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    private String displayName(MemberEntity member, UserView user) {
        if (member.getDefaultName() != null && !member.getDefaultName().isBlank()) {
            return member.getDefaultName();
        }
        if (user != null && user.username() != null && !user.username().isBlank()) {
            return user.username();
        }
        return null;
    }

    private void safePublish(DomainEvent event) {
        try {
            domainEventPublisher.publish(event);
        } catch (RuntimeException e) {
            log.warn("Failed to publish family plan domain event {}", event.getClass().getName(), e);
        }
    }

    private UserView currentUser(Authentication authentication) {
        return familyAccessService.currentUser(authentication.getName());
    }

    private boolean hasAccess(Long userId, Long familyId) {
        return familyAccessService.hasFamilyAccess(userId, familyId);
    }

    private Range normalizeRange(LocalDateTime from, LocalDateTime to) {
        LocalDateTime normalizedFrom = from == null ? LocalDate.now().atStartOfDay() : from;
        LocalDateTime normalizedTo = to == null ? normalizedFrom.plusDays(DEFAULT_QUERY_RANGE_DAYS) : to;
        return new Range(normalizedFrom, normalizedTo);
    }

    private String validateInput(String title, String description, LocalDateTime busyFrom, LocalDateTime busyTo) {
        if (title == null) {
            return "title is required";
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            return "title is too long";
        }
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            return "description is too long";
        }
        return validateRange(busyFrom, busyTo);
    }

    private String validateRange(LocalDateTime from, LocalDateTime to) {
        if (from == null) {
            return "busyFrom is required";
        }
        if (to == null) {
            return "busyTo is required";
        }
        if (!from.isBefore(to)) {
            return "busyFrom must be before busyTo";
        }
        if (Duration.between(from, to).toDays() > MAX_QUERY_RANGE_DAYS) {
            return "range is too long";
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private FamilyPlanListResp listTrue(List<FamilyPlanResp> items) {
        return new FamilyPlanListResp(true, "", items, LocalDateTime.now());
    }

    private FamilyPlanListResp listFalse(String reason) {
        return new FamilyPlanListResp(false, reason, List.of(), LocalDateTime.now());
    }

    private FamilyPlanResultResp planTrue(FamilyPlanResp plan) {
        return new FamilyPlanResultResp(true, "", plan, LocalDateTime.now());
    }

    private FamilyPlanResultResp planFalse(String reason) {
        return new FamilyPlanResultResp(false, reason, null, LocalDateTime.now());
    }

    private FamilyPlanCreateResultResp createFalse(String reason) {
        return new FamilyPlanCreateResultResp(false, reason, null, null, LocalDateTime.now());
    }

    private FamilyPlanDeleteResp deleteFalse(String reason) {
        return new FamilyPlanDeleteResp(false, reason, LocalDateTime.now());
    }

    private record Range(LocalDateTime from, LocalDateTime to) {
    }
}
