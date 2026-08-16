package org.buratishkin.familyhub.address.category.dto;

public record CategoryCreateResp(
        boolean result,
        String reason,
        Long categoryId
) {
}
