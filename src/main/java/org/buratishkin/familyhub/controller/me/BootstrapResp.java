package org.buratishkin.familyhub.controller.me;

import java.time.Instant;
import java.util.List;

public record BootstrapResp(
        Long userId,
        String username,
        String email,
        List<FamilySummary> families,
        Instant serverTime
) {
    public record FamilySummary(
            Long familyId,
            String familyName,
            String familyDescription,
            String myRole,
            List<MemberSummary> members,
            int memberCount,
            int userCount,
            int tasksToDoCount,
            int tasksCreatedByMeCount,
            List<AliasSummary> myAliases
    ) {
    }

    public record MemberSummary(
            Long memberId,
            String name
    ) {
    }

    public record AliasSummary(
            Long aliasId,
            Long targetMemberId,
            String alias
    ) {
    }
}
