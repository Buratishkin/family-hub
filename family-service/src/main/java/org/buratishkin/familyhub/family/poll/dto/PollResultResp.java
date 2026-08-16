package org.buratishkin.familyhub.family.poll.dto;

import java.time.LocalDateTime;

public record PollResultResp(
        boolean result,
        String reason,
        PollResp poll,
        LocalDateTime serverTime
) {
}
