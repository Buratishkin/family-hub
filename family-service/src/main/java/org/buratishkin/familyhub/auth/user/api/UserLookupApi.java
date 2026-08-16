package org.buratishkin.familyhub.auth.user.api;

public interface UserLookupApi {
    UserView findUserById(Long id);

    UserView findUserByUsername(String username);
}
