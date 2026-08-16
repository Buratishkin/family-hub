package org.buratishkin.familyhub.auth.user.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.UserEntity;
import org.buratishkin.familyhub.auth.user.UserRepository;
import org.buratishkin.familyhub.auth.user.api.UserLookupApi;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.auth.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCrudService implements UserLookupApi {
    private final UserRepository userRepository;

    public UserEntity findById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Нет пользователя с id: " + id));
    }

    public boolean existsByUsernameOrEmail(String username, String email){
        return existsByEmail(email) || existsByUsername(username);
    }

    public UserEntity save(UserEntity userEntity){
        return userRepository.save(userEntity);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }


    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public UserEntity findByLogin(String login) {
        if (existsByUsername(login))
            return findByUsername(login);
        else if (existsByEmail(login))
            return findByEmail(login);
        else
            throw new UserNotFoundException("нет пользователя с username/email: " + login);
    }

    public UserEntity findByUsername(String username){
        return userRepository.findByUsername(username);
    }
    public UserEntity findByEmail(String email){
        return userRepository.findByEmailIgnoreCase(email);
    }

    public UserView findUserById(Long id) {
        return toView(findById(id));
    }

    public UserView findUserByLogin(String login) {
        return toView(findByLogin(login));
    }

    public UserView findUserByUsername(String username) {
        return toView(findByUsername(username));
    }

    private UserView toView(UserEntity user) {
        if (user == null) {
            return null;
        }
        return new UserView(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
