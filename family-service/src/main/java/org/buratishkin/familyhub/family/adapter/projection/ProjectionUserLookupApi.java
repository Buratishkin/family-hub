package org.buratishkin.familyhub.family.adapter.projection;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserLookupApi;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.projection.FamilyUserProjectionEntity;
import org.buratishkin.familyhub.family.projection.FamilyUserProjectionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectionUserLookupApi implements UserLookupApi {
    private final FamilyUserProjectionRepository userRepository;

    @Override
    public UserView findUserById(Long id) {
        FamilyUserProjectionEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User projection is missing id: " + id));
        return toView(user);
    }

    @Override
    public UserView findUserByUsername(String username) {
        FamilyUserProjectionEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User projection is missing username: " + username));
        return toView(user);
    }

    private UserView toView(FamilyUserProjectionEntity user) {
        return new UserView(user.getUserId(), user.getUsername(), user.getEmail());
    }
}
