package org.buratishkin.familyhub.family.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.family.dto.FamilyCreateBodyReq;
import org.buratishkin.familyhub.family.dto.FamilyCreateResp;
import org.buratishkin.familyhub.family.member.dto.OfflineMemberCreateReq;
import org.buratishkin.familyhub.family.member.dto.OfflineMemberCreateResp;
import org.buratishkin.familyhub.family.service.FamilyManageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/family")
public class FamilyController {
    private final FamilyManageService familyManageService;

    @PostMapping("/")
    public CreateResp<FamilyCreateResp> create(@Valid @RequestBody FamilyCreateBodyReq createReq,
                                               Authentication authentication) {
        return familyManageService.create(createReq, authentication);
    }

    @DeleteMapping("/{familyId}")
    public DeleteResp delete(@PathVariable Long familyId,
                             Authentication authentication) {
        return familyManageService.delete(familyId, authentication);
    }

    @PostMapping("/{familyId}/members")
    public CreateResp<OfflineMemberCreateResp> createOfflineMember(@PathVariable Long familyId,
                                                                   @Valid @RequestBody OfflineMemberCreateReq createReq,
                                                                   Authentication authentication) {
        return familyManageService.createOfflineMember(familyId, createReq, authentication);
    }
}
