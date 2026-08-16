package org.buratishkin.familyhub.family.invite.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.invite.InviteEntity;
import org.buratishkin.familyhub.family.invite.InviteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InviteCrudService {
    private final InviteRepository inviteRepository;

    public InviteEntity save(InviteEntity invite) {
        return inviteRepository.save(invite);
    }

    public Optional<InviteEntity> findActiveByCodeHash(String codeHash, LocalDateTime now) {
        return inviteRepository.findFirstByCodeHashOrderByCreatedAtDesc(codeHash)
                .filter(invite -> invite.getRevokedAt() == null)
                .filter(invite -> invite.getExpiresAt().isAfter(now));
    }

    public Optional<InviteEntity> findActiveByTokenHash(String tokenHash, LocalDateTime now) {
        return inviteRepository.findFirstByTokenHashOrderByCreatedAtDesc(tokenHash)
                .filter(invite -> invite.getRevokedAt() == null)
                .filter(invite -> invite.getExpiresAt().isAfter(now));
    }

    public Optional<InviteEntity> findByCodeHash(String codeHash) {
        return inviteRepository.findFirstByCodeHashOrderByCreatedAtDesc(codeHash);
    }

    public Optional<InviteEntity> findByTokenHash(String tokenHash) {
        return inviteRepository.findFirstByTokenHashOrderByCreatedAtDesc(tokenHash);
    }

    public int revokeActiveByFamilyId(Long familyId, LocalDateTime now) {
        return inviteRepository.revokeActiveInvitesByFamilyId(familyId, now, now);
    }
}
