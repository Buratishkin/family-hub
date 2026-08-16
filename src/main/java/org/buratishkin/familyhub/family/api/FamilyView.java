package org.buratishkin.familyhub.family.api;

public record FamilyView(
        Long id,
        String name,
        String description,
        Long adminMemberId
) {
}
