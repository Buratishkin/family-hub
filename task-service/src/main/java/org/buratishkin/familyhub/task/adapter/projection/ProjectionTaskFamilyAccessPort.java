package org.buratishkin.familyhub.task.adapter.projection;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.task.adapter.http.FamilyServiceInternalClient;
import org.buratishkin.familyhub.task.port.TaskFamilyAccessPort;
import org.buratishkin.familyhub.task.projection.TaskDependencyProjectionService;
import org.buratishkin.familyhub.task.projection.TaskMemberProjectionRepository;
import org.buratishkin.familyhub.task.projection.TaskUserProjectionEntity;
import org.buratishkin.familyhub.task.projection.TaskUserProjectionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectionTaskFamilyAccessPort implements TaskFamilyAccessPort {
    private final TaskUserProjectionRepository userRepository;
    private final TaskMemberProjectionRepository memberRepository;
    private final TaskDependencyProjectionService projectionService;
    private final FamilyServiceInternalClient familyServiceInternalClient;

    @Override
    public UserView currentUser(String username) {
        TaskUserProjectionEntity user = userRepository.findByUsername(username).orElseGet(() -> {
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
        MemberView member = familyServiceInternalClient.findMemberByFamilyIdAndUserId(familyId, userId);
        projectionService.applyMemberAdded(member.familyId(), member.id(), member.userId());
        return true;
    }
}
