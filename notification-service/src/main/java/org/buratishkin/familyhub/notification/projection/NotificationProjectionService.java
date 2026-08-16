package org.buratishkin.familyhub.notification.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationProjectionService {
    private final NotificationUserProjectionRepository userRepository;
    private final NotificationMemberProjectionRepository memberRepository;

    @Transactional
    public void applyUserRegistered(Long userId, String username, String email) {
        if (userId == null || username == null) {
            return;
        }

        NotificationUserProjectionEntity user = userRepository.findByUserId(userId)
                .orElseGet(NotificationUserProjectionEntity::new);
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        userRepository.save(user);
    }

    @Transactional
    public void applyMemberAdded(Long familyId, Long memberId, Long userId) {
        if (familyId == null || memberId == null) {
            return;
        }

        NotificationMemberProjectionEntity member = memberRepository.findByMemberId(memberId)
                .orElseGet(NotificationMemberProjectionEntity::new);
        member.setFamilyId(familyId);
        member.setMemberId(memberId);
        member.setUserId(userId);
        member.setActive(true);
        memberRepository.save(member);
    }

    @Transactional
    public void applyMemberRemoved(Long memberId) {
        memberRepository.findByMemberId(memberId).ifPresent(member -> {
            member.setActive(false);
            memberRepository.save(member);
        });
    }

    @Transactional
    public void applyFamilyDeleted(Long familyId) {
        for (NotificationMemberProjectionEntity member : memberRepository.findAllByFamilyIdAndActiveTrue(familyId)) {
            member.setActive(false);
            memberRepository.save(member);
        }
    }

    public Set<Long> findActiveUserIdsByFamilyId(Long familyId) {
        Set<Long> userIds = new LinkedHashSet<>();
        for (NotificationMemberProjectionEntity member : memberRepository.findAllByFamilyIdAndActiveTrue(familyId)) {
            if (member.getUserId() != null) {
                userIds.add(member.getUserId());
            }
        }
        return userIds;
    }

    public Optional<Long> findUserIdByMemberId(Long memberId) {
        return memberRepository.findByMemberId(memberId)
                .filter(NotificationMemberProjectionEntity::isActive)
                .map(NotificationMemberProjectionEntity::getUserId);
    }

    public Optional<Long> findUserIdByUsername(String username) {
        return userRepository.findByUsername(username).map(NotificationUserProjectionEntity::getUserId);
    }
}
