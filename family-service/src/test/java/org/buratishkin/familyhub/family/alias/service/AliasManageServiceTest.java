package org.buratishkin.familyhub.family.alias.service;

import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.FamilyEntity;
import org.buratishkin.familyhub.family.alias.AliasEntity;
import org.buratishkin.familyhub.family.alias.dto.AliasCreateReq;
import org.buratishkin.familyhub.family.alias.dto.AliasCreateResp;
import org.buratishkin.familyhub.family.mapper.AliasMapper;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.service.MemberCrudService;
import org.buratishkin.familyhub.family.service.FamilyAccessService;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AliasManageServiceTest {
    @Mock
    private AliasMapper aliasMapper;

    @Mock
    private FamilyAccessService familyAccessService;

    @Mock
    private MemberCrudService memberCrudService;

    @Mock
    private AliasCrudService aliasCrudService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AliasManageService aliasManageService;

    @Test
    void createAllowsAliasForCurrentMember() {
        FamilyEntity family = family(10L);
        MemberEntity owner = member(100L, family);
        AliasEntity alias = alias(501L, owner, owner, "Me");

        when(authentication.getName()).thenReturn("user");
        when(familyAccessService.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(familyAccessService.hasFamilyAccess(1L, 10L)).thenReturn(true);
        when(memberCrudService.findByFamilyIdAndUserId(10L, 1L)).thenReturn(owner);
        when(memberCrudService.findById(100L)).thenReturn(owner);
        when(aliasMapper.toEntity("Me", owner, owner)).thenReturn(alias);

        CreateResp<AliasCreateResp> response = aliasManageService.create(
                new AliasCreateReq(10L, 100L, "Me"),
                authentication
        );

        assertThat(response.isResult()).isTrue();
        assertThat(response.getReason()).isEqualTo("good data");
        assertThat(response.getCreateResp().aliasId()).isEqualTo(501L);
        verify(aliasCrudService).save(alias);
        verify(domainEventPublisher).publish(any());
    }

    @Test
    void createRejectsAliasForMemberFromAnotherFamily() {
        FamilyEntity ownerFamily = family(10L);
        FamilyEntity targetFamily = family(11L);
        MemberEntity owner = member(100L, ownerFamily);
        MemberEntity target = member(101L, targetFamily);

        when(authentication.getName()).thenReturn("user");
        when(familyAccessService.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(familyAccessService.hasFamilyAccess(1L, 10L)).thenReturn(true);
        when(memberCrudService.findByFamilyIdAndUserId(10L, 1L)).thenReturn(owner);
        when(memberCrudService.findById(101L)).thenReturn(target);

        CreateResp<AliasCreateResp> response = aliasManageService.create(
                new AliasCreateReq(10L, 101L, "Other"),
                authentication
        );

        assertThat(response.isResult()).isFalse();
        assertThat(response.getReason()).isEqualTo("invalid credentials");
        assertThat(response.getCreateResp()).isNull();
        verify(aliasCrudService, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    private static FamilyEntity family(Long id) {
        FamilyEntity family = new FamilyEntity();
        family.setId(id);
        family.setName("Family " + id);
        return family;
    }

    private static MemberEntity member(Long id, FamilyEntity family) {
        MemberEntity member = new MemberEntity();
        member.setId(id);
        member.setFamily(family);
        member.setUserId(1L);
        return member;
    }

    private static AliasEntity alias(Long id, MemberEntity owner, MemberEntity target, String name) {
        AliasEntity alias = new AliasEntity();
        alias.setId(id);
        alias.setOwnerPerson(owner);
        alias.setTargetPerson(target);
        alias.setAlias(name);
        return alias;
    }
}
