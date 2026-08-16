package org.buratishkin.familyhub.meal.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MealDependencyProjectionService {
    private final MealUserProjectionRepository userRepository;
    private final MealMemberProjectionRepository memberRepository;

    @Transactional
    public void applyUserRegistered(Long userId, String username, String email) {
        MealUserProjectionEntity user = userRepository.findById(userId)
                .orElseGet(MealUserProjectionEntity::new);
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        userRepository.save(user);
    }

    @Transactional
    public void applyMemberAdded(Long familyId, Long memberId, Long userId) {
        MealMemberProjectionEntity member = memberRepository.findById(memberId)
                .orElseGet(MealMemberProjectionEntity::new);
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
    }
}
