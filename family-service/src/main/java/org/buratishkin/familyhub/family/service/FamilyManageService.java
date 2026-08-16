package org.buratishkin.familyhub.family.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.family.FamilyEntity;
import org.buratishkin.familyhub.family.api.event.FamilyDeletedEvent;
import org.buratishkin.familyhub.family.dto.FamilyCreateBodyReq;
import org.buratishkin.familyhub.family.dto.FamilyCreateReq;
import org.buratishkin.familyhub.family.dto.FamilyCreateResp;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.dto.OfflineMemberCreateReq;
import org.buratishkin.familyhub.family.member.dto.OfflineMemberCreateResp;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class FamilyManageService {
    private final FamilyManagerService familyManagerService;
    private final FamilyCrudService familyCrudService;
    private final FamilyAccessService familyAccessService;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public CreateResp<FamilyCreateResp> create(FamilyCreateBodyReq createReq, Authentication authentication) {
        UserView user = familyAccessService.currentUser(authentication.getName());

        FamilyCreateReq req = new FamilyCreateReq(createReq.name(), user.id());
        req.setDescription(createReq.description());
        FamilyEntity family = familyManagerService.createFamily(req);

        return getCreateTrueResult(new FamilyCreateResp(
                family.getId(),
                family.getAdmin() == null ? null : family.getAdmin().getId()
        ));
    }

    @Transactional
    public DeleteResp delete(Long familyId, Authentication authentication) {
        UserView user = familyAccessService.currentUser(authentication.getName());

        DeleteResp access = validateFamilyAccess(user.id(), familyId, this::getDeleteFalseResult);
        if (access != null) {
            return access;
        }

        FamilyEntity family = familyCrudService.findById(familyId);
        familyCrudService.deleteFamily(family);
        domainEventPublisher.publish(new FamilyDeletedEvent(
                familyId,
                user.id(),
                LocalDateTime.now()
        ));
        return getDeleteTrueResult();
    }

    @Transactional
    public CreateResp<OfflineMemberCreateResp> createOfflineMember(Long familyId,
                                                                   OfflineMemberCreateReq createReq,
                                                                   Authentication authentication) {
        UserView user = familyAccessService.currentUser(authentication.getName());

        CreateResp<OfflineMemberCreateResp> access = validateFamilyAccess(
                user.id(),
                familyId,
                this::getOfflineMemberCreateFalseResult
        );
        if (access != null) {
            return access;
        }

        MemberEntity member = familyManagerService.addOfflineMemberInFamily(familyId, createReq.name());
        return getOfflineMemberCreateTrueResult(new OfflineMemberCreateResp(
                member.getId(),
                familyId,
                member.getDefaultName()
        ));
    }

    private <T> T validateFamilyAccess(Long userId, Long familyId, Supplier<T> falseResultSupplier) {
        if (!familyAccessService.hasFamilyAccess(userId, familyId)) {
            return falseResultSupplier.get();
        }
        return null;
    }

    private CreateResp<FamilyCreateResp> getCreateTrueResult(FamilyCreateResp createResp) {
        return new CreateResp<>(
                true,
                "good data",
                createResp
        );
    }

    private CreateResp<OfflineMemberCreateResp> getOfflineMemberCreateFalseResult() {
        return new CreateResp<>(
                false,
                "invalid familyId",
                null
        );
    }

    private CreateResp<OfflineMemberCreateResp> getOfflineMemberCreateTrueResult(OfflineMemberCreateResp createResp) {
        return new CreateResp<>(
                true,
                "good data",
                createResp
        );
    }

    private DeleteResp getDeleteFalseResult() {
        return new DeleteResp(
                false,
                "invalid familyId"
        );
    }

    private DeleteResp getDeleteTrueResult() {
        return new DeleteResp(
                true,
                "good data"
        );
    }
}
