package org.buratishkin.familyhub.family.alias.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.shared.response.UpdateResp;
import org.buratishkin.familyhub.family.alias.dto.AliasCreateReq;
import org.buratishkin.familyhub.family.alias.dto.AliasCreateResp;
import org.buratishkin.familyhub.family.alias.dto.AliasDeleteReq;
import org.buratishkin.familyhub.family.alias.dto.AliasUpdateReq;
import org.buratishkin.familyhub.family.alias.service.AliasManageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alias")
@RequiredArgsConstructor
public class AliasController {
    private final AliasManageService aliasManageService;

    @PostMapping("/")
    public CreateResp<AliasCreateResp> create(@Valid @RequestBody AliasCreateReq createReq, Authentication authentication){
        return aliasManageService.create(createReq, authentication);
    }

    @DeleteMapping("/{aliasId}")
    public DeleteResp delete(@Valid @RequestBody AliasDeleteReq deleteReq,
                             @PathVariable Long aliasId,
                             Authentication authentication){
        return aliasManageService.delete(deleteReq, aliasId, authentication);
    }

    @PutMapping("/{aliasId}")
    public UpdateResp update(@Valid @RequestBody AliasUpdateReq updateReq,
                             @PathVariable Long aliasId,
                             Authentication authentication){
        return aliasManageService.update(updateReq, aliasId, authentication);
    }
}