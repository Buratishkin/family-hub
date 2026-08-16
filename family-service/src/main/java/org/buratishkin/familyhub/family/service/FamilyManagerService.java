package org.buratishkin.familyhub.family.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.FamilyEntity;
import org.buratishkin.familyhub.family.api.event.FamilyAdminChangedEvent;
import org.buratishkin.familyhub.family.api.event.FamilyCreatedEvent;
import org.buratishkin.familyhub.family.api.event.FamilyDeletedEvent;
import org.buratishkin.familyhub.family.dto.FamilyCreateReq;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.api.event.MemberAddedEvent;
import org.buratishkin.familyhub.family.member.api.event.MemberRemovedEvent;
import org.buratishkin.familyhub.family.member.enums.MemberRole;
import org.buratishkin.familyhub.family.member.dto.MemberDeleteResp;
import org.buratishkin.familyhub.family.member.service.MemberCrudService;
import org.buratishkin.familyhub.auth.user.api.UserLookupApi;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.mapper.FamilyMapper;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FamilyManagerService {
    private final UserLookupApi userCrudService;
    private final MemberCrudService memberCrudService;
    private final FamilyCrudService familyCrudService;
    private final FamilyMapper familyMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public FamilyEntity createFamily(FamilyCreateReq createReq){
        FamilyEntity family = familyMapper.toEntity(createReq);
        FamilyEntity savedFamily = familyCrudService.save(family);

        UserView user = userCrudService.findUserById(createReq.getAdminId());
        MemberEntity admin = new MemberEntity(savedFamily, user.id());
        admin.setRole(MemberRole.ADMIN);
        admin.setJoinTime(LocalDateTime.now());

        savedFamily.getMembers().add(admin);
        savedFamily.setAdmin(admin);
        FamilyEntity savedFamilyWithAdmin = familyCrudService.saveAndFlush(savedFamily);
        Long adminMemberId = savedFamilyWithAdmin.getAdmin() == null ? null : savedFamilyWithAdmin.getAdmin().getId();
        domainEventPublisher.publish(new FamilyCreatedEvent(
                savedFamilyWithAdmin.getId(),
                adminMemberId,
                user.id(),
                LocalDateTime.now()
        ));
        domainEventPublisher.publish(new MemberAddedEvent(
                savedFamilyWithAdmin.getId(),
                adminMemberId,
                user.id(),
                LocalDateTime.now()
        ));
        return savedFamilyWithAdmin;
    }

    @Transactional
    public boolean addMemberInFamily(FamilyEntity family, Long userId){
        if (!memberCrudService.existsByFamilyIdAndMemberId(family.getId(), userId)){
            MemberEntity memberEntity = new MemberEntity(family, userId);
            memberEntity.setRole(MemberRole.MEMBER);
            memberEntity.setJoinTime(LocalDateTime.now());

            family.getMembers().add(memberEntity);
            familyCrudService.saveAndFlush(family);
            Long memberId = memberEntity.getId();
            domainEventPublisher.publish(new MemberAddedEvent(
                    family.getId(),
                    memberId,
                    userId,
                    LocalDateTime.now()
            ));

            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean addMemberInFamily(Long familyId, Long userId){
        FamilyEntity family = familyCrudService.findById(familyId);
        userCrudService.findUserById(userId);
        return addMemberInFamily(family, userId);
    }

    @Transactional
    public MemberEntity addOfflineMemberInFamily(Long familyId, String name) {
        FamilyEntity family = familyCrudService.findById(familyId);
        MemberEntity member = new MemberEntity(family, null);
        member.setRole(MemberRole.MEMBER);
        member.setDefaultName(name.trim());
        member.setJoinTime(LocalDateTime.now());

        family.getMembers().add(member);
        familyCrudService.saveAndFlush(family);
        domainEventPublisher.publish(new MemberAddedEvent(
                family.getId(),
                member.getId(),
                null,
                LocalDateTime.now()
        ));
        return member;
    }

    @Transactional
    public MemberDeleteResp removeMemberFromFamily(Long memberId){
        if (!memberCrudService.existsById(memberId))
            return new MemberDeleteResp(false, null);

        MemberEntity member = memberCrudService.findById(memberId);
        FamilyEntity family = member.getFamily();
        Long familyId = family.getId();
        Long userId = member.getUserId();

        if (family.getAdmin().getId().equals(memberId))
            swapAdmin(family.getId());

        family.getMembers().remove(member);
        memberCrudService.removeByEntity(member);
        domainEventPublisher.publish(new MemberRemovedEvent(
                familyId,
                memberId,
                userId,
                LocalDateTime.now()
        ));

        if (family.getMembers().isEmpty()){
            familyCrudService.deleteFamily(family);
            domainEventPublisher.publish(new FamilyDeletedEvent(
                    familyId,
                    userId,
                    LocalDateTime.now()
            ));
            return new MemberDeleteResp(true, null);
        }

        return new MemberDeleteResp(true, familyCrudService.save(family));
    }

    public boolean swapAdmin(Long familyId){
        FamilyEntity family = familyCrudService.findById(familyId);

        if (family.getMembers().size() == 1)
            return false;

        MemberEntity oldAdmin = family.getAdmin();
        MemberEntity newAdmin = null;

        for (MemberEntity member : family.getMembers()){
            if (oldAdmin != null && oldAdmin.getId().equals(member.getId())) {
                continue;
            }
            if (member.getJoinTime() == null) {
                continue;
            }
            if (newAdmin == null || member.getJoinTime().isBefore(newAdmin.getJoinTime())) {
                newAdmin = member;
            }
        }

        if (newAdmin == null) {
            for (MemberEntity member : family.getMembers()) {
                if (oldAdmin != null && oldAdmin.getId().equals(member.getId())) {
                    continue;
                }
                newAdmin = member;
                break;
            }
        }

        if (newAdmin == null) {
            return false;
        }

        if (oldAdmin != null) {
            oldAdmin.setRole(MemberRole.MEMBER);
        }
        return changeAdmin(familyId, newAdmin.getId());
    }

    @Transactional
    public boolean changeAdmin(Long familyId, Long newAdminMemberId) {
        FamilyEntity family = familyCrudService.findById(familyId);
        MemberEntity currentAdmin = family.getAdmin();
        Long previousAdminMemberId = currentAdmin == null ? null : currentAdmin.getId();
        MemberEntity newAdmin = memberCrudService.findById(newAdminMemberId);

        if (newAdmin.getFamily() == null || !familyId.equals(newAdmin.getFamily().getId())) {
            return false;
        }

        if (currentAdmin != null && currentAdmin.getId().equals(newAdmin.getId())) {
            return true;
        }

        if (currentAdmin != null) {
            currentAdmin.setRole(MemberRole.MEMBER);
        }

        newAdmin.setRole(MemberRole.ADMIN);
        family.setAdmin(newAdmin);
        familyCrudService.save(family);
        domainEventPublisher.publish(new FamilyAdminChangedEvent(
                familyId,
                previousAdminMemberId,
                newAdmin.getId(),
                LocalDateTime.now()
        ));
        return true;
    }
}
