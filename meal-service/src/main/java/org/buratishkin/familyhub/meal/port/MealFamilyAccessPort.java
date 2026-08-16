package org.buratishkin.familyhub.meal.port;

import org.buratishkin.familyhub.auth.user.api.UserView;

public interface MealFamilyAccessPort {
    UserView currentUser(String username);

    boolean hasFamilyAccess(Long userId, Long familyId);
}
