package org.buratishkin.familyhub.family.poll.dto;

public record PollAnswerCreateReq(
        Long familyId,
        Long pollId,
        String text
) {
}
