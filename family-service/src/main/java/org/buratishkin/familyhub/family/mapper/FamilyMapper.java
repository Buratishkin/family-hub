package org.buratishkin.familyhub.family.mapper;

import org.buratishkin.familyhub.shared.mapper.Mapper;

import org.buratishkin.familyhub.family.FamilyEntity;
import org.buratishkin.familyhub.family.dto.FamilyCreateReq;
import org.springframework.stereotype.Component;

@Component
public class FamilyMapper implements Mapper<FamilyCreateReq, FamilyEntity> {
    @Override
    public FamilyEntity toEntity(FamilyCreateReq source) {
        FamilyEntity family = new FamilyEntity();
        family.setName(source.getName());
        family.setDescription(source.getDescription());
        return family;
    }
}
