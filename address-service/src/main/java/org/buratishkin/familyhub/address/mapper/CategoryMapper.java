package org.buratishkin.familyhub.address.mapper;

import org.buratishkin.familyhub.shared.mapper.Mapper;

import org.buratishkin.familyhub.address.category.CategoryEntity;
import org.buratishkin.familyhub.address.category.CategoryType;
import org.buratishkin.familyhub.address.category.dto.CategoryCreateReq;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CategoryMapper implements Mapper<CategoryCreateReq, CategoryEntity> {
    @Override
    public CategoryEntity toEntity(CategoryCreateReq source) {
        CategoryEntity category = new CategoryEntity();
        category.setName(source.name().trim().toLowerCase());
        category.setType(CategoryType.CUSTOM);
        category.setArchived(false);
        category.setCreatedAt(Instant.now());
        return category;
    }

    public CategoryEntity toEntity(CategoryCreateReq source, Long familyId) {
        CategoryEntity category = toEntity(source);
        category.setOwnerFamilyId(familyId);
        return category;
    }
}
