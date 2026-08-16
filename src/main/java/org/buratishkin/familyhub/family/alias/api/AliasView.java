package org.buratishkin.familyhub.family.alias.api;

public record AliasView(
        Long id,
        Long ownerMemberId,
        Long targetMemberId,
        String alias
) {
}
