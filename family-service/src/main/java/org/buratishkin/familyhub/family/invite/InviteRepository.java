package org.buratishkin.familyhub.family.invite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<InviteEntity, Long> {

    Optional<InviteEntity> findFirstByCodeHashOrderByCreatedAtDesc(String codeHash);

    Optional<InviteEntity> findFirstByTokenHashOrderByCreatedAtDesc(String tokenHash);

    @Modifying
    @Query("""
           update InviteEntity i
           set i.revokedAt = :revokedAt
           where i.family.id = :familyId
             and i.revokedAt is null
             and i.expiresAt > :now
           """)
    int revokeActiveInvitesByFamilyId(
            @Param("familyId") Long familyId,
            @Param("now") LocalDateTime now,
            @Param("revokedAt") LocalDateTime revokedAt
    );

}
