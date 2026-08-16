package org.buratishkin.familyhub.family.alias.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.shared.response.UpdateResp;
import org.buratishkin.familyhub.family.alias.AliasEntity;
import org.buratishkin.familyhub.family.alias.api.event.AliasCreatedEvent;
import org.buratishkin.familyhub.family.alias.api.event.AliasDeletedEvent;
import org.buratishkin.familyhub.family.alias.api.event.AliasUpdatedEvent;
import org.buratishkin.familyhub.family.alias.dto.AliasCreateReq;
import org.buratishkin.familyhub.family.alias.dto.AliasCreateResp;
import org.buratishkin.familyhub.family.alias.dto.AliasDeleteReq;
import org.buratishkin.familyhub.family.alias.dto.AliasUpdateReq;
import org.buratishkin.familyhub.family.service.FamilyAccessService;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.service.MemberCrudService;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.mapper.AliasMapper;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AliasManageService {
    private final AliasMapper aliasMapper;
    private final FamilyAccessService familyAccessService;
    private final MemberCrudService memberCrudService;
    private final AliasCrudService aliasCrudService;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public CreateResp<AliasCreateResp> create(AliasCreateReq createReq, Authentication authentication){
        UserView user = getCurrentUser(authentication);
        CreateResp<AliasCreateResp> access = validateFamilyAccess(
                user,
                createReq.familyId(),
                this::getCreateFalseResult
        );
        if (access != null) {
            return access;
        }
        MemberEntity owner = memberCrudService.findByFamilyIdAndUserId(createReq.familyId(), user.id());
        MemberEntity target = memberCrudService.findById(createReq.targetId());

        // если люди из разных семей, то им нельзя менять ники друг для друга
        if (!isSameFamily(owner, target)){
            return getCreateFalseResult();
        }

        AliasEntity alias = aliasMapper.toEntity(createReq.alias(), owner, target);
        aliasCrudService.save(alias);
        domainEventPublisher.publish(new AliasCreatedEvent(
                alias.getId(),
                owner.getFamily().getId(),
                owner.getId(),
                target.getId(),
                LocalDateTime.now()
        ));

        return getCreateTrueResult(new AliasCreateResp(alias.getId()));
    }

    @Transactional
    public DeleteResp delete(AliasDeleteReq deleteReq, Long aliasId, Authentication authentication){
        UserView user = getCurrentUser(authentication);
        DeleteResp access = validateFamilyAccess(
                user,
                deleteReq.familyId(),
                this::getDeleteFalseResult
        );
        if (access != null) {
            return access;
        }

        MemberEntity owner = memberCrudService.findByFamilyIdAndUserId(deleteReq.familyId(), user.id());
        AliasEntity alias = aliasCrudService.findById(aliasId);

        if (!isOwnerOfAlias(owner, alias)) {
            return getDeleteFalseResult();
        }
        Long targetMemberId = alias.getTargetPerson().getId();
        aliasCrudService.delete(alias);
        domainEventPublisher.publish(new AliasDeletedEvent(
                aliasId,
                deleteReq.familyId(),
                owner.getId(),
                targetMemberId,
                LocalDateTime.now()
        ));
        return getDeleteTrueResult();
    }

    @Transactional
    public UpdateResp update(AliasUpdateReq updateReq, Long aliasId, Authentication authentication){
        UserView user = getCurrentUser(authentication);
        UpdateResp access = validateFamilyAccess(
                user,
                updateReq.familyId(),
                this::getUpdateFalseResult
        );
        if (access != null) {
            return access;
        }

        MemberEntity owner = memberCrudService.findByFamilyIdAndUserId(updateReq.familyId(), user.id());
        AliasEntity alias = aliasCrudService.findById(aliasId);

        if (!isOwnerOfAlias(owner, alias)) {
            return getUpdateFalseResult();
        }

        alias.setAlias(updateReq.alias());
        aliasCrudService.save(alias);
        domainEventPublisher.publish(new AliasUpdatedEvent(
                alias.getId(),
                updateReq.familyId(),
                owner.getId(),
                alias.getTargetPerson().getId(),
                LocalDateTime.now()
        ));
        return getUpdateTrueResult();
    }

    private UserView getCurrentUser(Authentication authentication) {
        return familyAccessService.currentUser(authentication.getName());
    }

    private boolean isSameFamily(MemberEntity owner, MemberEntity target) {
        return Objects.equals(owner.getFamily().getId(), target.getFamily().getId());
    }

    private boolean isOwnerOfAlias(MemberEntity owner, AliasEntity alias) {
        return Objects.equals(alias.getOwnerPerson().getId(), owner.getId());
    }

    private <T> T validateFamilyAccess(UserView user, Long familyId, Supplier<T> falseResultSupplier) {
        if (!familyAccessService.hasFamilyAccess(user.id(), familyId)) {
            return falseResultSupplier.get();
        }
        return null;
    }

    private CreateResp<AliasCreateResp> getCreateFalseResult() {
        return new CreateResp<>(
                false,
                "invalid credentials",
                null
        );
    }

    private CreateResp<AliasCreateResp> getCreateTrueResult(AliasCreateResp createResp) {
        return new CreateResp<>(
                true,
                "good data",
                createResp
        );
    }

    private DeleteResp getDeleteFalseResult() {
        return new DeleteResp(
                false,
                "invalid credentials"
        );
    }

    private DeleteResp getDeleteTrueResult() {
        return new DeleteResp(
                true,
                "good data"
        );
    }

    private UpdateResp getUpdateFalseResult() {
        return new UpdateResp(
                false,
                "invalid credentials"
        );
    }

    private UpdateResp getUpdateTrueResult() {
        return new UpdateResp(
                true,
                "good data"
        );
    }
}
