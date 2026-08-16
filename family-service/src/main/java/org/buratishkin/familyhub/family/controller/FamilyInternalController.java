package org.buratishkin.familyhub.family.controller;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.api.FamilyAccessApi;
import org.buratishkin.familyhub.family.api.FamilyView;
import org.buratishkin.familyhub.family.service.FamilyCrudService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/families")
public class FamilyInternalController {
    private final FamilyCrudService familyCrudService;
    private final FamilyAccessApi familyAccessApi;

    @GetMapping("/{familyId}")
    public FamilyView findFamilyById(@PathVariable Long familyId) {
        return familyCrudService.findFamilyById(familyId);
    }

    @GetMapping("/{familyId}/exists")
    public boolean existsById(@PathVariable Long familyId) {
        return familyCrudService.existsById(familyId);
    }

    @GetMapping("/{familyId}/access/users/{userId}")
    public boolean hasFamilyAccess(@PathVariable Long familyId, @PathVariable Long userId) {
        return familyAccessApi.hasFamilyAccess(userId, familyId);
    }

    @GetMapping("/current-user/{username}")
    public UserView currentUser(@PathVariable String username) {
        return familyAccessApi.currentUser(username);
    }
}
