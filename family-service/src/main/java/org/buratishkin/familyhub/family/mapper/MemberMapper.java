package org.buratishkin.familyhub.family.mapper;

import org.buratishkin.familyhub.shared.mapper.Mapper;

import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.dto.MemberCreateReq;
import org.buratishkin.familyhub.family.member.enums.MemberRole;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MemberMapper implements Mapper<MemberCreateReq, MemberEntity> {
    @Override
    public MemberEntity toEntity(MemberCreateReq source) {
        MemberEntity member = new MemberEntity();
        member.setDefaultName(source.getDefaultName());
        member.setRole(source.getRole() == null ? MemberRole.MEMBER : source.getRole());
        member.setJoinTime(LocalDateTime.now());
        return member;
    }

    public MemberEntity toEntity(MemberCreateReq source, Long userId) {
        MemberEntity member = toEntity(source);
        member.setUserId(userId);
        return member;
    }
}
