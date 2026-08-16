package org.buratishkin.familyhub.family.service;

import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.dto.OfflineMemberCreateReq;
import org.buratishkin.familyhub.family.member.dto.OfflineMemberCreateResp;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyManageServiceTest {
    @Mock
    private FamilyManagerService familyManagerService;

    @Mock
    private FamilyAccessService familyAccessService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FamilyManageService familyManageService;

    @Test
    void createOfflineMemberCreatesMemberWithoutUserAccount() {
        when(authentication.getName()).thenReturn("user");
        when(familyAccessService.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(familyAccessService.hasFamilyAccess(1L, 10L)).thenReturn(true);

        MemberEntity member = new MemberEntity();
        member.setId(100L);
        member.setUserId(null);
        member.setDefaultName("Grandma");
        when(familyManagerService.addOfflineMemberInFamily(10L, "Grandma")).thenReturn(member);

        CreateResp<OfflineMemberCreateResp> response = familyManageService.createOfflineMember(
                10L,
                new OfflineMemberCreateReq("Grandma"),
                authentication
        );

        assertThat(response.isResult()).isTrue();
        assertThat(response.getReason()).isEqualTo("good data");
        assertThat(response.getCreateResp().memberId()).isEqualTo(100L);
        assertThat(response.getCreateResp().familyId()).isEqualTo(10L);
        assertThat(response.getCreateResp().name()).isEqualTo("Grandma");
        verify(familyManagerService).addOfflineMemberInFamily(10L, "Grandma");
    }

    @Test
    void createOfflineMemberRejectsInaccessibleFamily() {
        when(authentication.getName()).thenReturn("user");
        when(familyAccessService.currentUser("user")).thenReturn(new UserView(1L, "user", "user@example.com"));
        when(familyAccessService.hasFamilyAccess(1L, 10L)).thenReturn(false);

        CreateResp<OfflineMemberCreateResp> response = familyManageService.createOfflineMember(
                10L,
                new OfflineMemberCreateReq("Grandma"),
                authentication
        );

        assertThat(response.isResult()).isFalse();
        assertThat(response.getReason()).isEqualTo("invalid familyId");
        assertThat(response.getCreateResp()).isNull();
        verify(familyManagerService, never()).addOfflineMemberInFamily(10L, "Grandma");
    }
}
