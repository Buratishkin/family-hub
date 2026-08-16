package org.buratishkin.familyhub.meal.recipe.api.event;

import org.buratishkin.familyhub.shared.event.DomainEvent;

import java.time.LocalDateTime;

public record RecipeCreatedEvent(
        Long recipeId,
        Long familyId,
        Long createdByUserId,
        String recipeName,
        LocalDateTime occurredAt
) implements DomainEvent {
}
