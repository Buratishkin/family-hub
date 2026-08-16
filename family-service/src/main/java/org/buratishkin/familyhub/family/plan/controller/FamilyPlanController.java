package org.buratishkin.familyhub.family.plan.controller;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanCreateReq;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanCreateResultResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanDeleteResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanListResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanResultResp;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanUpdateReq;
import org.buratishkin.familyhub.family.plan.service.FamilyPlanService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/me/families/{familyId}/plans")
public class FamilyPlanController {
    private final FamilyPlanService familyPlanService;

    @GetMapping
    public FamilyPlanListResp list(@PathVariable Long familyId,
                                   @RequestParam(required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                   LocalDateTime from,
                                   @RequestParam(required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                   LocalDateTime to,
                                   @RequestParam(required = false) Long memberId,
                                   Authentication authentication) {
        return familyPlanService.list(familyId, from, to, memberId, authentication);
    }

    @GetMapping("/{planId}")
    public FamilyPlanResultResp find(@PathVariable Long familyId,
                                     @PathVariable Long planId,
                                     Authentication authentication) {
        return familyPlanService.find(familyId, planId, authentication);
    }

    @PostMapping
    public FamilyPlanCreateResultResp create(@PathVariable Long familyId,
                                             @RequestBody(required = false) FamilyPlanCreateReq req,
                                             Authentication authentication) {
        return familyPlanService.create(familyId, req, authentication);
    }

    @PutMapping("/{planId}")
    public FamilyPlanResultResp update(@PathVariable Long familyId,
                                       @PathVariable Long planId,
                                       @RequestBody(required = false) FamilyPlanUpdateReq req,
                                       Authentication authentication) {
        return familyPlanService.update(familyId, planId, req, authentication);
    }

    @DeleteMapping("/{planId}")
    public FamilyPlanDeleteResp delete(@PathVariable Long familyId,
                                       @PathVariable Long planId,
                                       Authentication authentication) {
        return familyPlanService.delete(familyId, planId, authentication);
    }
}
