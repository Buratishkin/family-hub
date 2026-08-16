package org.buratishkin.familyhub.family.poll.dto;

import java.time.LocalDateTime;

public record PollAnswerResp(
        Long id,
        Long pollId,
        Long memberId,
        String memberName,
        String text,
        LocalDateTime createdAt
) {
}
