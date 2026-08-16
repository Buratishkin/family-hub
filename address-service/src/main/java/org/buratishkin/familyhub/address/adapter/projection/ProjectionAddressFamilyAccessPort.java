package org.buratishkin.familyhub.address.adapter.projection;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.address.adapter.http.FamilyServiceInternalClient;
import org.buratishkin.familyhub.address.port.AddressFamilyAccessPort;
import org.buratishkin.familyhub.address.projection.AddressDependencyProjectionService;
import org.buratishkin.familyhub.address.projection.AddressFamilyProjectionRepository;
import org.buratishkin.familyhub.address.projection.AddressMemberProjectionRepository;
import org.buratishkin.familyhub.address.projection.AddressUserProjectionEntity;
import org.buratishkin.familyhub.address.projection.AddressUserProjectionRepository;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectionAddressFamilyAccessPort implements AddressFamilyAccessPort {
    private final AddressUserProjectionRepository userRepository;
    private final AddressFamilyProjectionRepository familyRepository;
    private final AddressMemberProjectionRepository memberRepository;
    private final AddressDependencyProjectionService projectionService;
    private final FamilyServiceInternalClient familyServiceInternalClient;

    @Override
    public UserView currentUser(String username) {
        AddressUserProjectionEntity user = userRepository.findByUsername(username).orElseGet(() -> {
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
        if (familyExists(familyId) && memberRepository.existsByFamilyIdAndUserId(familyId, userId)) {
            return true;
        }

        if (!familyServiceInternalClient.hasFamilyAccess(userId, familyId)) {
            return false;
        }
        FamilyServiceInternalClient.MemberView member =
                familyServiceInternalClient.findMemberByFamilyIdAndUserId(familyId, userId);
        cacheFamily(familyId);
        projectionService.applyMemberAdded(member.familyId(), member.id(), member.userId());
        return true;
    }

    @Override
    public boolean familyExists(Long familyId) {
        if (familyId == null) {
            return false;
        }
        if (familyRepository.existsById(familyId)) {
            return true;
        }
        if (!familyServiceInternalClient.familyExists(familyId)) {
            return false;
        }
        cacheFamily(familyId);
        return true;
    }

    private void cacheFamily(Long familyId) {
        FamilyServiceInternalClient.FamilyView family = familyServiceInternalClient.findFamilyById(familyId);
        Long adminUserId = null;
        if (family.adminMemberId() != null) {
            adminUserId = familyServiceInternalClient.findMemberById(family.adminMemberId()).userId();
        }
        projectionService.applyFamilyCreated(family.id(), family.adminMemberId(), adminUserId);
    }
}
