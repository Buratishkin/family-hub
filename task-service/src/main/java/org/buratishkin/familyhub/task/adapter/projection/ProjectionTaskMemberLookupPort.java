package org.buratishkin.familyhub.task.adapter.projection;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.member.api.MemberView;
import org.buratishkin.familyhub.task.adapter.http.FamilyServiceInternalClient;
import org.buratishkin.familyhub.task.port.TaskMemberLookupPort;
import org.buratishkin.familyhub.task.projection.TaskDependencyProjectionService;
import org.buratishkin.familyhub.task.projection.TaskMemberProjectionEntity;
import org.buratishkin.familyhub.task.projection.TaskMemberProjectionRepository;
import org.buratishkin.familyhub.task.projection.TaskUserProjectionEntity;
import org.buratishkin.familyhub.task.projection.TaskUserProjectionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectionTaskMemberLookupPort implements TaskMemberLookupPort {
    private final TaskMemberProjectionRepository memberRepository;
    private final TaskUserProjectionRepository userRepository;
    private final TaskDependencyProjectionService projectionService;
    private final FamilyServiceInternalClient familyServiceInternalClient;

    @Override
    public MemberView findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .map(this::toView)
                .orElseGet(() -> fetchAndCacheMember(memberId));
    }

    @Override
    public MemberView findMemberByFamilyIdAndUserId(Long familyId, Long userId) {
        return memberRepository.findByFamilyIdAndUserId(familyId, userId)
                .map(this::toView)
                .orElseGet(() -> fetchAndCacheMember(familyId, userId));
    }

    @Override
    public List<MemberView> findMembersByFamilyId(Long familyId) {
        return memberRepository.findAllByFamilyId(familyId).stream()
                .map(this::toView)
                .toList();
    }

    private MemberView toView(TaskMemberProjectionEntity member) {
        String username = member.getUserId() == null
                ? null
                : userRepository.findById(member.getUserId())
                        .map(TaskUserProjectionEntity::getUsername)
                        .orElse(null);
        return new MemberView(
                member.getMemberId(),
                member.getFamilyId(),
                member.getUserId(),
                username,
                null,
                null,
                null
        );
    }

    private MemberView fetchAndCacheMember(Long memberId) {
        try {
            MemberView member = familyServiceInternalClient.findMemberById(memberId);
            cacheMember(member);
            return member;
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private MemberView fetchAndCacheMember(Long familyId, Long userId) {
        try {
            MemberView member = familyServiceInternalClient.findMemberByFamilyIdAndUserId(familyId, userId);
            cacheMember(member);
            return member;
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private void cacheMember(MemberView member) {
        if (member != null) {
            projectionService.applyMemberAdded(member.familyId(), member.id(), member.userId());
        }
    }
}
