package org.buratishkin.familyhub.family.plan.service;

import org.buratishkin.familyhub.auth.user.api.UserLookupApi;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.MemberRepository;
import org.buratishkin.familyhub.family.member.service.MemberCrudService;
import org.buratishkin.familyhub.family.plan.FamilyPlanEntity;
import org.buratishkin.familyhub.family.plan.FamilyPlanRepository;
import org.buratishkin.familyhub.family.plan.api.event.FamilyPlanCreatedEvent;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanCreateReq;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanCreateResultResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanListResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanResultResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanUpdateReq;
import org.buratishkin.familyhub.family.service.FamilyAccessService;
import org.buratishkin.familyhub.shared.event.DomainEvent;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyPlanServiceTest {
    @Mock
    private FamilyPlanRepository planRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberCrudService memberCrudService;

    @Mock
    private FamilyAccessService familyAccessService;

    @Mock
    private UserLookupApi userLookupApi;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FamilyPlanService familyPlanService;

    @Test
    void createDerivesMemberFromCurrentUser() {
        LocalDateTime from = LocalDateTime.of(2026, 8, 12, 18, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 12, 19, 30);
        whenCurrentUserHasAccess(1L, 10L);

        MemberEntity member = member(77L, 1L, "Masha");
        when(memberCrudService.findByFamilyIdAndUserId(10L, 1L)).thenReturn(member);
        when(planRepository.save(any(FamilyPlanEntity.class))).thenAnswer(invocation -> {
            FamilyPlanEntity plan = invocation.getArgument(0);
            plan.setId(1001L);
            return plan;
        });
        when(memberRepository.findAllById(any())).thenReturn(List.of(member));

        FamilyPlanCreateResultResp response = familyPlanService.create(
                10L,
                new FamilyPlanCreateReq("English lesson", "Busy", from, to),
                authentication
        );

        assertThat(response.result()).isTrue();
        assertThat(response.createResp().planId()).isEqualTo(1001L);
        assertThat(response.plan().memberId()).isEqualTo(77L);
        assertThat(response.plan().memberName()).isEqualTo("Masha");

        ArgumentCaptor<FamilyPlanEntity> captor = ArgumentCaptor.forClass(FamilyPlanEntity.class);
        verify(planRepository).save(captor.capture());
        assertThat(captor.getValue().getFamilyId()).isEqualTo(10L);
        assertThat(captor.getValue().getMemberId()).isEqualTo(77L);
        assertThat(captor.getValue().getTitle()).isEqualTo("English lesson");
        assertThat(captor.getValue().getBusyFrom()).isEqualTo(from);
        assertThat(captor.getValue().getBusyTo()).isEqualTo(to);

        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(FamilyPlanCreatedEvent.class);
        FamilyPlanCreatedEvent event = (FamilyPlanCreatedEvent) eventCaptor.getValue();
        assertThat(event.familyPlanId()).isEqualTo(1001L);
        assertThat(event.familyId()).isEqualTo(10L);
        assertThat(event.memberId()).isEqualTo(77L);
        assertThat(event.actorUserId()).isEqualTo(1L);
        assertThat(event.title()).isEqualTo("English lesson");
    }

    @Test
    void listUsesOverlapQueryAndOptionalMemberFilter() {
        LocalDateTime from = LocalDateTime.of(2026, 8, 12, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 13, 0, 0);
        FamilyPlanEntity plan = plan(1001L, 10L, 77L, from.plusHours(18), from.plusHours(19));
        whenCurrentUserHasAccess(1L, 10L);
        when(memberCrudService.existsByIdAndFamilyId(77L, 10L)).thenReturn(true);
        when(planRepository.findAllByFamilyIdAndMemberIdAndBusyFromLessThanAndBusyToGreaterThanOrderByBusyFromAscIdAsc(
                10L,
                77L,
                to,
                from
        )).thenReturn(List.of(plan));
        when(memberRepository.findAllById(any())).thenReturn(List.of(member(77L, 1L, "Masha")));

        FamilyPlanListResp response = familyPlanService.list(10L, from, to, 77L, authentication);

        assertThat(response.result()).isTrue();
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().id()).isEqualTo(1001L);
        verify(planRepository).findAllByFamilyIdAndMemberIdAndBusyFromLessThanAndBusyToGreaterThanOrderByBusyFromAscIdAsc(
                10L,
                77L,
                to,
                from
        );
    }

    @Test
    void findInternalConflictsUsesInclusiveStartExclusiveEndPointQuery() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 12, 14, 30);
        FamilyPlanEntity plan = plan(
                1001L,
                10L,
                77L,
                LocalDateTime.of(2026, 8, 12, 14, 0),
                LocalDateTime.of(2026, 8, 12, 15, 0)
        );
        when(planRepository.findAllByFamilyIdAndMemberIdAndBusyFromLessThanEqualAndBusyToGreaterThanOrderByBusyFromAscIdAsc(
                10L,
                77L,
                start,
                start
        )).thenReturn(List.of(plan));
        when(memberRepository.findAllById(any())).thenReturn(List.of(member(77L, 1L, "Masha")));

        var conflicts = familyPlanService.findInternalConflicts(10L, 77L, start);

        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.getFirst().id()).isEqualTo(1001L);
        assertThat(conflicts.getFirst().busyFrom()).isEqualTo(LocalDateTime.of(2026, 8, 12, 14, 0));
        assertThat(conflicts.getFirst().busyTo()).isEqualTo(LocalDateTime.of(2026, 8, 12, 15, 0));
    }

    @Test
    void updateRejectsPlanOwnedByAnotherMember() {
        LocalDateTime from = LocalDateTime.of(2026, 8, 12, 18, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 12, 19, 0);
        whenCurrentUserHasAccess(1L, 10L);
        when(planRepository.findByIdAndFamilyId(1001L, 10L)).thenReturn(Optional.of(plan(1001L, 10L, 99L, from, to)));
        when(memberCrudService.findByFamilyIdAndUserId(10L, 1L)).thenReturn(member(77L, 1L, "Masha"));

        FamilyPlanResultResp response = familyPlanService.update(
                10L,
                1001L,
                new FamilyPlanUpdateReq("New title", null, null, null),
                authentication
        );

        assertThat(response.result()).isFalse();
        assertThat(response.reason()).isEqualTo("only owner can change plan");
        verify(planRepository, never()).save(any(FamilyPlanEntity.class));
    }

    private void whenCurrentUserHasAccess(Long userId, Long familyId) {
        when(authentication.getName()).thenReturn("user");
        when(familyAccessService.currentUser("user")).thenReturn(new UserView(userId, "user", "user@example.com"));
        when(familyAccessService.hasFamilyAccess(userId, familyId)).thenReturn(true);
    }

    private MemberEntity member(Long memberId, Long userId, String name) {
        MemberEntity member = new MemberEntity();
        member.setId(memberId);
        member.setUserId(userId);
        member.setDefaultName(name);
        return member;
    }

    private FamilyPlanEntity plan(Long planId,
                                  Long familyId,
                                  Long memberId,
                                  LocalDateTime busyFrom,
                                  LocalDateTime busyTo) {
        FamilyPlanEntity plan = new FamilyPlanEntity();
        plan.setId(planId);
        plan.setFamilyId(familyId);
        plan.setMemberId(memberId);
        plan.setTitle("Busy");
        plan.setBusyFrom(busyFrom);
        plan.setBusyTo(busyTo);
        plan.setCreatedAt(busyFrom.minusDays(1));
        plan.setUpdatedAt(busyFrom.minusDays(1));
        return plan;
    }
}
