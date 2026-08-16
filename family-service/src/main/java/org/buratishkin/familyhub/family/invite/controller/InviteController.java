package org.buratishkin.familyhub.family.invite.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.family.invite.dto.InviteCreateReq;
import org.buratishkin.familyhub.family.invite.dto.InviteCreateResp;
import org.buratishkin.familyhub.family.invite.dto.InviteRedeemReq;
import org.buratishkin.familyhub.family.invite.dto.InviteRedeemResp;
import org.buratishkin.familyhub.family.invite.service.InviteManageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invite")
public class InviteController {
    private final InviteManageService inviteManageService;

    @PostMapping("/")
    public CreateResp<InviteCreateResp> create(@Valid @RequestBody InviteCreateReq createReq,
                                               Authentication authentication) {
        return inviteManageService.create(createReq, authentication);
    }

    @PostMapping("/redeem")
    public CreateResp<InviteRedeemResp> redeem(@Valid @RequestBody InviteRedeemReq redeemReq,
                                               Authentication authentication) {
        return inviteManageService.redeem(redeemReq, authentication);
    }
}
