package org.buratishkin.familyhub.family.mapper;

import org.buratishkin.familyhub.shared.mapper.Mapper;

import org.buratishkin.familyhub.family.alias.AliasEntity;
import org.buratishkin.familyhub.family.alias.dto.AliasCreateReq;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.springframework.stereotype.Component;

@Component
public class AliasMapper implements Mapper<AliasCreateReq, AliasEntity> {
    @Override
    public AliasEntity toEntity(AliasCreateReq source) {
        AliasEntity alias = new AliasEntity();
        alias.setAlias(source.alias());
        return alias;
    }

    public AliasEntity toEntity(String aliasText, MemberEntity owner, MemberEntity target) {
        AliasEntity alias = new AliasEntity();
        alias.setOwnerPerson(owner);
        alias.setTargetPerson(target);
        alias.setAlias(aliasText);
        return alias;
    }
}
