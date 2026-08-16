package org.buratishkin.familyhub.family.poll.dto;

public record PollCreateReq(
        Long familyId,
        String question,
        Integer ttlDays
) {
}
