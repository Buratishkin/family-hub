package org.buratishkin.familyhub.task.port;

import org.buratishkin.familyhub.auth.user.api.UserView;

public interface TaskFamilyAccessPort {
    UserView currentUser(String username);

    boolean hasFamilyAccess(Long userId, Long familyId);
}
