package org.buratishkin.familyhub.family.member.dto;

import jakarta.validation.constraints.NotBlank;

public record OfflineMemberCreateReq(
        @NotBlank
        String name
) {
}
