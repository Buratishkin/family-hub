package org.buratishkin.familyhub.family.poll.dto;

import org.buratishkin.familyhub.family.poll.PollStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PollResp(
        Long id,
        Long familyId,
        String question,
        Long creatorMemberId,
        String creatorName,
        PollStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime closedAt,
        List<PollAnswerResp> answers
) {
}
