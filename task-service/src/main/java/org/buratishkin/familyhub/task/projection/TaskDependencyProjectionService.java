package org.buratishkin.familyhub.task.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskDependencyProjectionService {
    private final TaskUserProjectionRepository userRepository;
    private final TaskMemberProjectionRepository memberRepository;
    private final TaskAddressProjectionRepository addressRepository;

    @Transactional
    public void applyUserRegistered(Long userId, String username, String email) {
        TaskUserProjectionEntity user = userRepository.findById(userId)
                .orElseGet(TaskUserProjectionEntity::new);
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        userRepository.save(user);
    }

    @Transactional
    public void applyMemberAdded(Long familyId, Long memberId, Long userId) {
        TaskMemberProjectionEntity member = memberRepository.findById(memberId)
                .orElseGet(TaskMemberProjectionEntity::new);
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
        addressRepository.deleteAllByFamilyId(familyId);
    }

    @Transactional
    public void applyAddressChanged(Long addressId, Long familyId, Long categoryId) {
        TaskAddressProjectionEntity address = addressRepository.findById(addressId)
                .orElseGet(TaskAddressProjectionEntity::new);
        address.setAddressId(addressId);
        address.setFamilyId(familyId);
        address.setCategoryId(categoryId);
        addressRepository.save(address);
    }

    @Transactional
    public void applyAddressDeleted(Long addressId) {
        addressRepository.deleteById(addressId);
    }
}
