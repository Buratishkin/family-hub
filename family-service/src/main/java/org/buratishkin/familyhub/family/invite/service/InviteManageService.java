package org.buratishkin.familyhub.family.invite.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.family.FamilyEntity;
import org.buratishkin.familyhub.family.api.FamilyAccessApi;
import org.buratishkin.familyhub.family.service.FamilyCrudService;
import org.buratishkin.familyhub.family.service.FamilyManagerService;
import org.buratishkin.familyhub.family.invite.InviteEntity;
import org.buratishkin.familyhub.family.invite.InviteRedemptionEntity;
import org.buratishkin.familyhub.family.invite.api.event.InviteCreatedEvent;
import org.buratishkin.familyhub.family.invite.api.event.InviteRedeemedEvent;
import org.buratishkin.familyhub.family.invite.dto.InviteCreateReq;
import org.buratishkin.familyhub.family.invite.dto.InviteCreateResp;
import org.buratishkin.familyhub.family.invite.dto.InviteRedeemReq;
import org.buratishkin.familyhub.family.invite.dto.InviteRedeemResp;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.family.member.service.MemberCrudService;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.auth.security.service.TokenHashService;
import org.buratishkin.familyhub.family.mapper.InviteMapper;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InviteManageService {
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final int TOKEN_BYTES = 24;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final InviteCrudService inviteCrudService;
    private final InviteRedemptionCrudService inviteRedemptionCrudService;
    private final FamilyCrudService familyCrudService;
    private final FamilyManagerService familyManagerService;
    private final FamilyAccessApi familyAccessService;
    private final MemberCrudService memberCrudService;
    private final TokenHashService tokenHashService;
    private final InviteMapper inviteMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Value("${family-hub.invite.ttl-minutes:15}")
    private long inviteTtlMinutes;

    @Value("${family-hub.invite.deep-link-base:familyhub://join}")
    private String deepLinkBase;

    @Value("${family-hub.invite.public-join-url:http://localhost:5173/invite}")
    private String publicJoinUrl;

    @Transactional
    public CreateResp<InviteCreateResp> create(InviteCreateReq req, Authentication authentication) {
        UserView user = familyAccessService.currentUser(authentication.getName());
        if (!familyAccessService.hasFamilyAccess(user.id(), req.familyId())) {
            return getCreateInviteFalseResult();
        }

        FamilyEntity family = familyCrudService.findById(req.familyId());
        MemberEntity creator = memberCrudService.findByFamilyIdAndUserId(req.familyId(), user.id());
        LocalDateTime now = LocalDateTime.now();

        inviteCrudService.revokeActiveByFamilyId(req.familyId(), now);

        String code = generateCode();
        String token = generateToken();
        InviteEntity invite = inviteMapper.toEntity(
                family,
                creator,
                tokenHashService.sha256(code),
                tokenHashService.sha256(token),
                now,
                now.plusMinutes(inviteTtlMinutes)
        );
        invite = inviteCrudService.save(invite);
        domainEventPublisher.publish(new InviteCreatedEvent(
                invite.getId(),
                family.getId(),
                creator.getId(),
                invite.getExpiresAt(),
                LocalDateTime.now()
        ));

        InviteCreateResp resp = new InviteCreateResp(
                invite.getId(),
                code,
                buildInviteLink(deepLinkBase, token),
                buildInviteLink(publicJoinUrl, token),
                invite.getExpiresAt()
        );
        return getCreateInviteTrueResult(resp);
    }

    @Transactional
    public CreateResp<InviteRedeemResp> redeem(InviteRedeemReq req, Authentication authentication) {
        UserView user = familyAccessService.currentUser(authentication.getName());

        LocalDateTime now = LocalDateTime.now();
        Optional<InviteEntity> inviteOptional = findInvite(req);
        if (inviteOptional.isEmpty()) {
            return getRedeemFalseResult("invite not found");
        }

        InviteEntity invite = inviteOptional.get();
        if (invite.getRevokedAt() != null) {
            return getRedeemFalseResult("invite revoked");
        }
        if (!invite.getExpiresAt().isAfter(now)) {
            return getRedeemFalseResult("invite expired");
        }

        Long familyId = invite.getFamily().getId();
        if (memberCrudService.existsByFamilyIdAndUserId(familyId, user.id())) {
            return getRedeemFalseResult("user already in this family");
        }

        boolean added = familyManagerService.addMemberInFamily(invite.getFamily(), user.id());
        if (!added) {
            return getRedeemFalseResult("user already in this family");
        }

        MemberView member = memberCrudService.findMemberByFamilyIdAndUserId(familyId, user.id());
        InviteRedemptionEntity redemption = new InviteRedemptionEntity();
        redemption.setInvite(invite);
        redemption.setMember(memberCrudService.findById(member.id()));
        redemption.setUserId(user.id());
        redemption.setRedeemedAt(now);
        inviteRedemptionCrudService.save(redemption);
        domainEventPublisher.publish(new InviteRedeemedEvent(
                invite.getId(),
                familyId,
                member.id(),
                user.id(),
                LocalDateTime.now()
        ));
        return getRedeemTrueResult(new InviteRedeemResp(familyId, member.id()));
    }

    private Optional<InviteEntity> findInvite(InviteRedeemReq req) {
        if (req == null) {
            return Optional.empty();
        }

        String code = normalize(req.code());
        String token = normalize(req.token());
        if (code == null && token == null) {
            return Optional.empty();
        }
        if (code != null && token != null) {
            return Optional.empty();
        }

        if (code != null) {
            return inviteCrudService.findByCodeHash(tokenHashService.sha256(code));
        }
        return inviteCrudService.findByTokenHash(tokenHashService.sha256(token));
    }

    private String buildInviteLink(String baseUrl, String token) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized;
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(CODE_CHARS.length());
            code.append(CODE_CHARS.charAt(index));
        }
        return code.toString();
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private CreateResp<InviteCreateResp> getCreateInviteTrueResult(InviteCreateResp createResp) {
        return new CreateResp<>(
                true,
                "good data",
                createResp
        );
    }

    private CreateResp<InviteCreateResp> getCreateInviteFalseResult() {
        return new CreateResp<>(
                false,
                "invalid credentials",
                null
        );
    }

    private CreateResp<InviteRedeemResp> getRedeemTrueResult(InviteRedeemResp createResp) {
        return new CreateResp<>(
                true,
                "good data",
                createResp
        );
    }

    private CreateResp<InviteRedeemResp> getRedeemFalseResult(String reason) {
        return new CreateResp<>(
                false,
                reason,
                null
        );
    }
}
