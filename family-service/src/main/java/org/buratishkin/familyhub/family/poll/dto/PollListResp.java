package org.buratishkin.familyhub.family.poll.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PollListResp(
        boolean result,
        String reason,
        List<PollResp> items,
        LocalDateTime serverTime
) {
}
