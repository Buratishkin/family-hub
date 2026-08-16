package org.buratishkin.familyhub.family.poll.dto;

import java.time.LocalDateTime;

public record PollAnswerResultResp(
        boolean result,
        String reason,
        PollAnswerResp answer,
        LocalDateTime serverTime
) {
}
