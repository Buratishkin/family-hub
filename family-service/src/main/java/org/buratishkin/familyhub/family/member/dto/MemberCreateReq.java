package org.buratishkin.familyhub.family.member.dto;

import lombok.Getter;
import lombok.Setter;
import org.buratishkin.familyhub.family.member.enums.MemberRole;

@Setter
@Getter
public class MemberCreateReq {

    public MemberCreateReq(Long familyId, Long userId){
        this.familyId = familyId;
        this.userId = userId;
    }

    Long familyId;
    Long userId;
    MemberRole role;
    String defaultName;
}
