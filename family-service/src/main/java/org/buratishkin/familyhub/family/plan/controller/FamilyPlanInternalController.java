package org.buratishkin.familyhub.family.plan.controller;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.plan.dto.FamilyPlanResp;
import org.buratishkin.familyhub.family.plan.service.FamilyPlanService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/plans")
public class FamilyPlanInternalController {
    private final FamilyPlanService familyPlanService;

    @GetMapping("/families/{familyId}/members/{memberId}/conflicts")
    public List<FamilyPlanResp> findConflicts(@PathVariable Long familyId,
                                              @PathVariable Long memberId,
                                              @RequestParam
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                              LocalDateTime start) {
        return familyPlanService.findInternalConflicts(familyId, memberId, start);
    }
}
