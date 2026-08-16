package org.buratishkin.familyhub.family.dto;

import lombok.*;

@Setter
@Getter
public class FamilyCreateReq {
    public FamilyCreateReq(String name, Long adminId){
        this.name = name;
        this.adminId = adminId;
    }

    String name;
    Long adminId;

    String description;
}
