package org.buratishkin.familyhub.address.mapper;

import org.buratishkin.familyhub.shared.mapper.Mapper;

import org.buratishkin.familyhub.address.AddressEntity;
import org.buratishkin.familyhub.address.dto.AddressCreateReq;
import org.buratishkin.familyhub.address.dto.AddressUpdateReq;
import org.buratishkin.familyhub.address.enums.StreetType;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper implements Mapper<AddressCreateReq, AddressEntity> {
    @Override
    public AddressEntity toEntity(AddressCreateReq source) {
        AddressEntity address = new AddressEntity();
        StreetType streetType = parseStreetType(source.streetType());
        address.setName(source.name());
        address.setCountry(source.country());
        address.setCity(source.city());
        address.setStreetType(streetType);
        address.setStreet(source.street());
        address.setHouse(source.house());
        address.setApartment(source.apartment());
        address.setComment(source.comment());
        return address;
    }

    public AddressEntity toEntity(AddressCreateReq source, Long familyId, Long categoryId) {
        AddressEntity address = toEntity(source);
        address.setFamilyId(familyId);
        address.setCategoryId(categoryId);
        return address;
    }

    public void applyUpdate(AddressUpdateReq source,
                            AddressEntity target,
                            Long categoryId,
                            StreetType streetType) {
        target.setCategoryId(categoryId);
        target.setName(source.name());
        target.setCountry(source.country());
        target.setCity(source.city());
        target.setStreetType(streetType);
        target.setStreet(source.street());
        target.setHouse(source.house());
        target.setApartment(source.apartment());
        target.setComment(source.comment());
    }

    public StreetType parseStreetType(String streetType) {
        if (streetType == null || streetType.isBlank()) {
            return null;
        }
        String normalizedStreetType = streetType.trim();
        for (StreetType type : StreetType.values()) {
            if (type.getRussianName().equalsIgnoreCase(normalizedStreetType)) {
                return type;
            }
        }
        try {
            return StreetType.valueOf(normalizedStreetType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
