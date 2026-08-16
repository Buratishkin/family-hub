package org.buratishkin.familyhub.family.invite.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.invite.InviteRedemptionEntity;
import org.buratishkin.familyhub.family.invite.InviteRedemptionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InviteRedemptionCrudService {
    private final InviteRedemptionRepository inviteRedemptionRepository;

    public InviteRedemptionEntity save(InviteRedemptionEntity redemption) {
        return inviteRedemptionRepository.save(redemption);
    }
}
