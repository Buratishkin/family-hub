package org.buratishkin.familyhub.address.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressDependencyProjectionService {
    private final AddressUserProjectionRepository userRepository;
    private final AddressFamilyProjectionRepository familyRepository;
    private final AddressMemberProjectionRepository memberRepository;

    @Transactional
    public void applyUserRegistered(Long userId, String username, String email) {
        AddressUserProjectionEntity user = userRepository.findById(userId)
                .orElseGet(AddressUserProjectionEntity::new);
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        userRepository.save(user);
    }

    @Transactional
    public void applyFamilyCreated(Long familyId, Long adminMemberId, Long adminUserId) {
        AddressFamilyProjectionEntity family = familyRepository.findById(familyId)
                .orElseGet(AddressFamilyProjectionEntity::new);
        family.setFamilyId(familyId);
        family.setAdminMemberId(adminMemberId);
        family.setAdminUserId(adminUserId);
        familyRepository.save(family);
    }

    @Transactional
    public void applyMemberAdded(Long familyId, Long memberId, Long userId) {
        AddressMemberProjectionEntity member = memberRepository.findById(memberId)
                .orElseGet(AddressMemberProjectionEntity::new);
        member.setMemberId(memberId);
        member.setFamilyId(familyId);
        member.setUserId(userId);
        memberRepository.save(member);
    }

    @Transactional
    public void applyMemberRemoved(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    @Transactional
    public void applyFamilyDeleted(Long familyId) {
        memberRepository.deleteAllByFamilyId(familyId);
        familyRepository.deleteById(familyId);
    }
}
