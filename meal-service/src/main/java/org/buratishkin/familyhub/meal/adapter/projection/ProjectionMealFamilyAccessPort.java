package org.buratishkin.familyhub.meal.adapter.projection;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.meal.adapter.http.FamilyServiceInternalClient;
import org.buratishkin.familyhub.meal.port.MealFamilyAccessPort;
import org.buratishkin.familyhub.meal.projection.MealDependencyProjectionService;
import org.buratishkin.familyhub.meal.projection.MealMemberProjectionRepository;
import org.buratishkin.familyhub.meal.projection.MealUserProjectionEntity;
import org.buratishkin.familyhub.meal.projection.MealUserProjectionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectionMealFamilyAccessPort implements MealFamilyAccessPort {
    private final MealUserProjectionRepository userRepository;
    private final MealMemberProjectionRepository memberRepository;
    private final MealDependencyProjectionService projectionService;
    private final FamilyServiceInternalClient familyServiceInternalClient;

    @Override
    public UserView currentUser(String username) {
        MealUserProjectionEntity user = userRepository.findByUsername(username).orElseGet(() -> {
            UserView remoteUser = familyServiceInternalClient.currentUser(username);
            projectionService.applyUserRegistered(remoteUser.id(), remoteUser.username(), remoteUser.email());
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User projection is missing username: " + username));
        });
        return new UserView(user.getUserId(), user.getUsername(), user.getEmail());
    }

    @Override
    public boolean hasFamilyAccess(Long userId, Long familyId) {
        if (userId == null || familyId == null) {
            return false;
        }
        if (memberRepository.existsByFamilyIdAndUserId(familyId, userId)) {
            return true;
        }

        if (!familyServiceInternalClient.hasFamilyAccess(userId, familyId)) {
            return false;
        }
        FamilyServiceInternalClient.MemberView member = familyServiceInternalClient.findMemberByFamilyIdAndUserId(familyId, userId);
        projectionService.applyMemberAdded(member.familyId(), member.id(), member.userId());
        return true;
    }
}
