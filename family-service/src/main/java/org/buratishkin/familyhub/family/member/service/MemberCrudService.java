package org.buratishkin.familyhub.family.member.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserLookupApi;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.MemberRepository;
import org.buratishkin.familyhub.family.member.api.MemberLookupApi;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.family.member.exception.MemberNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberCrudService implements MemberLookupApi {
    private final MemberRepository memberRepository;
    private final UserLookupApi userLookupApi;

    public MemberEntity save(MemberEntity member){
        return memberRepository.save(member);
    }

    public MemberEntity findByFamilyIdAndUserId(Long familyId, Long userId){
        return memberRepository.findFamilyMemberEntityByFamily_IdAndUserId(familyId, userId)
                .orElseThrow(() -> new MemberNotFoundException("Нет пользователя с id: " + userId + "в семье с id: " + familyId));
    }

    public MemberEntity findById(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Нет члена семьи с id: " + memberId));
    }

    public boolean existsByFamilyIdAndMemberId(Long familyId, Long userId){
        return existsByFamilyIdAndUserId(familyId, userId);
    }

    public boolean existsByFamilyIdAndUserId(Long familyId, Long userId){
        return memberRepository.existsByFamily_IdAndUserId(familyId, userId);
    }

    public List<MemberEntity> findAllByUserId(Long userId) {
        return memberRepository.findAllByUserId(userId);
    }

    public MemberView findMemberById(Long memberId) {
        return toView(findById(memberId));
    }

    public MemberView findMemberByFamilyIdAndUserId(Long familyId, Long userId) {
        return toView(findByFamilyIdAndUserId(familyId, userId));
    }

    public List<MemberView> findMembersByUserId(Long userId) {
        return findAllByUserId(userId).stream()
                .map(this::toView)
                .toList();
    }

    public List<MemberView> findMembersByFamilyId(Long familyId) {
        return memberRepository.findAllByFamily_Id(familyId).stream()
                .map(this::toView)
                .toList();
    }

    public void removeByEntity(MemberEntity fmEntity){
        memberRepository.delete(fmEntity);
    }

    public boolean existsById(Long memberId) {
        return memberRepository.existsById(memberId);
    }

    public boolean existsByUserId(Long userId) {
        return memberRepository.existsByUserId(userId);
    }

    public boolean existsByIdAndFamilyId(Long memberId, Long familyId) {
        return memberRepository.existsByIdAndFamily_Id(memberId, familyId);
    }

    private MemberView toView(MemberEntity member) {
        if (member == null) {
            return null;
        }
        UserView user = member.getUserId() == null ? null : userLookupApi.findUserById(member.getUserId());
        return new MemberView(
                member.getId(),
                member.getFamily() == null ? null : member.getFamily().getId(),
                member.getUserId(),
                user == null ? null : user.username(),
                member.getRole(),
                member.getDefaultName(),
                member.getJoinTime()
        );
    }
}
