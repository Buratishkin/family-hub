package org.buratishkin.familyhub.family.member.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.FamilyEntity;
import org.buratishkin.familyhub.family.service.FamilyCrudService;
import org.buratishkin.familyhub.family.service.FamilyManagerService;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.dto.MemberCreateReq;
import org.buratishkin.familyhub.family.member.dto.MemberCreateResp;
import org.buratishkin.familyhub.family.member.dto.MemberDeleteResp;
import org.buratishkin.familyhub.auth.user.api.UserLookupApi;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.mapper.MemberMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberManagerService {
    private final UserLookupApi userCrudService;
    private final FamilyManagerService familyManagerService;
    private final FamilyCrudService familyCrudService;
    private final MemberCrudService memberCrudService;
    private final MemberMapper memberMapper;

    public MemberCreateResp createMember(MemberCreateReq createReq){
        FamilyEntity family = familyCrudService.findById(createReq.getFamilyId());
        UserView user = userCrudService.findUserById(createReq.getUserId());

        if (memberCrudService.existsByFamilyIdAndMemberId(family.getId(), user.id()))
            return new MemberCreateResp(
                    false,
                    "user already exists",
                    null
            );

        MemberEntity member = memberMapper.toEntity(createReq, user.id());
        member.setFamily(family);

        return new MemberCreateResp(
                true,
                "user created",
                memberCrudService.save(member)
        );
    }

    public MemberDeleteResp deleteMember(Long memberId){
        return familyManagerService
                .removeMemberFromFamily(memberId);
    }
}
