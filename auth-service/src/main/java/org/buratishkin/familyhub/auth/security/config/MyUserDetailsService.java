package org.buratishkin.familyhub.auth.security.config;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.UserEntity;
import org.buratishkin.familyhub.auth.user.exception.UserNotFoundException;
import org.buratishkin.familyhub.auth.user.service.UserCrudService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserCrudService userCrudService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserEntity user = userCrudService.findByLogin(username);
            return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities("USER")
                    .build();
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("User not found: " + username, e);
        }
    }
}
