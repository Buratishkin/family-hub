package org.buratishkin.familyhub.family.member.controller;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.family.member.service.MemberCrudService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/members")
public class MemberInternalController {
    private final MemberCrudService memberCrudService;

    @GetMapping("/{memberId}")
    public MemberView findMemberById(@PathVariable Long memberId) {
        return memberCrudService.findMemberById(memberId);
    }

    @GetMapping("/families/{familyId}/users/{userId}")
    public MemberView findMemberByFamilyIdAndUserId(@PathVariable Long familyId, @PathVariable Long userId) {
        return memberCrudService.findMemberByFamilyIdAndUserId(familyId, userId);
    }

    @GetMapping("/users/{userId}")
    public List<MemberView> findMembersByUserId(@PathVariable Long userId) {
        return memberCrudService.findMembersByUserId(userId);
    }

    @GetMapping("/families/{familyId}")
    public List<MemberView> findMembersByFamilyId(@PathVariable Long familyId) {
        return memberCrudService.findMembersByFamilyId(familyId);
    }

    @GetMapping("/families/{familyId}/users/{userId}/exists")
    public boolean existsByFamilyIdAndUserId(@PathVariable Long familyId, @PathVariable Long userId) {
        return memberCrudService.existsByFamilyIdAndUserId(familyId, userId);
    }

    @GetMapping("/users/{userId}/exists")
    public boolean existsByUserId(@PathVariable Long userId) {
        return memberCrudService.existsByUserId(userId);
    }

    @GetMapping("/{memberId}/families/{familyId}/exists")
    public boolean existsByIdAndFamilyId(@PathVariable Long memberId, @PathVariable Long familyId) {
        return memberCrudService.existsByIdAndFamilyId(memberId, familyId);
    }
}
