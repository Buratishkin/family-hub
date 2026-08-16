package org.buratishkin.familyhub.family.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserLookupApi;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.api.FamilyAccessApi;
import org.buratishkin.familyhub.family.api.FamilyLookupApi;
import org.buratishkin.familyhub.family.member.api.MemberLookupApi;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FamilyAccessService implements FamilyAccessApi {
    private final FamilyLookupApi familyCrudService;
    private final UserLookupApi userCrudService;
    private final MemberLookupApi memberLookupApi;

    public UserView currentUser(String username) {
        return userCrudService.findUserByUsername(username);
    }

    public boolean hasFamilyAccess(Long userId, Long familyId) {
        if (userId == null || familyId == null || !familyCrudService.existsById(familyId)) {
            return false;
        }
        return memberLookupApi.existsByFamilyIdAndUserId(familyId, userId);
    }
}
