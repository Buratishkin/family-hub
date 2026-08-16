package org.buratishkin.familyhub.family.api;

import org.buratishkin.familyhub.auth.user.api.UserView;

public interface FamilyAccessApi {
    UserView currentUser(String username);

    boolean hasFamilyAccess(Long userId, Long familyId);
}
