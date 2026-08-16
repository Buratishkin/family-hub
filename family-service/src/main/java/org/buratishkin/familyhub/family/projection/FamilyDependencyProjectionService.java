package org.buratishkin.familyhub.family.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyDependencyProjectionService {
    private final FamilyUserProjectionRepository userRepository;

    @Transactional
    public void applyUserRegistered(Long userId, String username, String email) {
        FamilyUserProjectionEntity user = userRepository.findById(userId)
                .orElseGet(FamilyUserProjectionEntity::new);
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        userRepository.save(user);
    }
}
