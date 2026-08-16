package org.buratishkin.familyhub.address.category.api;

import java.time.Instant;

public record CategoryView(
        Long id,
        Long familyId,
        String name,
        String type,
        boolean archived,
        Instant createdAt
) {
}
