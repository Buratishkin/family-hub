package org.buratishkin.familyhub.address.api;

public record AddressView(
        Long id,
        Long familyId,
        Long categoryId,
        String name,
        String country,
        String city,
        String streetType,
        String street,
        String house,
        String apartment,
        String comment
) {
}
