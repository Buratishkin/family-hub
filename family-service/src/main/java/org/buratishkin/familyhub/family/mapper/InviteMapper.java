package org.buratishkin.familyhub.family.mapper;

import org.buratishkin.familyhub.shared.mapper.Mapper;

import org.buratishkin.familyhub.family.FamilyEntity;
import org.buratishkin.familyhub.family.invite.InviteEntity;
import org.buratishkin.familyhub.family.invite.dto.InviteCreateReq;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InviteMapper implements Mapper<InviteCreateReq, InviteEntity> {
    @Override
    public InviteEntity toEntity(InviteCreateReq source) {
        return new InviteEntity();
    }

    public InviteEntity toEntity(
            FamilyEntity family,
            MemberEntity createdBy,
            String codeHash,
            String tokenHash,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        InviteEntity invite = new InviteEntity();
        invite.setFamily(family);
        invite.setCreatedBy(createdBy);
        invite.setCodeHash(codeHash);
        invite.setTokenHash(tokenHash);
        invite.setCreatedAt(createdAt);
        invite.setExpiresAt(expiresAt);
        return invite;
    }
}
