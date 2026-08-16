package org.buratishkin.familyhub.family.invite.dto;

import jakarta.validation.constraints.NotNull;

public record InviteCreateReq(
        @NotNull
        Long familyId
) {
}
