package org.buratishkin.familyhub.family.poll.dto;

import java.time.LocalDateTime;

public record PollCreateResultResp(
        boolean result,
        String reason,
        PollCreateResp createResp,
        PollResp poll,
        LocalDateTime serverTime
) {
}
