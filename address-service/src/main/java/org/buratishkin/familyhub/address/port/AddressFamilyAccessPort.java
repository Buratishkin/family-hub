package org.buratishkin.familyhub.address.port;

import org.buratishkin.familyhub.auth.user.api.UserView;

public interface AddressFamilyAccessPort {
    UserView currentUser(String username);

    boolean hasFamilyAccess(Long userId, Long familyId);

    boolean familyExists(Long familyId);
}
