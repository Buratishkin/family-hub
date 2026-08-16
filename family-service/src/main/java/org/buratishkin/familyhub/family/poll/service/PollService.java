package org.buratishkin.familyhub.family.poll.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buratishkin.familyhub.auth.user.api.UserLookupApi;
import org.buratishkin.familyhub.auth.user.api.UserView;
import org.buratishkin.familyhub.family.member.MemberEntity;
import org.buratishkin.familyhub.family.member.MemberRepository;
import org.buratishkin.familyhub.family.member.exception.MemberNotFoundException;
import org.buratishkin.familyhub.family.member.service.MemberCrudService;
import org.buratishkin.familyhub.family.poll.PollAnswerEntity;
import org.buratishkin.familyhub.family.poll.PollAnswerRepository;
import org.buratishkin.familyhub.family.poll.PollEntity;
import org.buratishkin.familyhub.family.poll.PollRepository;
import org.buratishkin.familyhub.family.poll.PollStatus;
import org.buratishkin.familyhub.family.poll.api.event.PollCreatedEvent;
import org.buratishkin.familyhub.family.poll.dto.PollAnswerCreateReq;
import org.buratishkin.familyhub.family.poll.dto.PollAnswerResp;
import org.buratishkin.familyhub.family.poll.dto.PollAnswerResultResp;
import org.buratishkin.familyhub.family.poll.dto.PollCreateReq;
import org.buratishkin.familyhub.family.poll.dto.PollCreateResp;
import org.buratishkin.familyhub.family.poll.dto.PollCreateResultResp;
import org.buratishkin.familyhub.family.poll.dto.PollListResp;
import org.buratishkin.familyhub.family.poll.dto.PollResp;
import org.buratishkin.familyhub.family.poll.dto.PollResultResp;
import org.buratishkin.familyhub.family.service.FamilyAccessService;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollService {
    private static final int POLL_TTL_DAYS = 7;
    private static final int MAX_QUESTION_LENGTH = 1000;
    private static final int MAX_ANSWER_LENGTH = 4000;

    private final PollRepository pollRepository;
    private final PollAnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final MemberCrudService memberCrudService;
    private final FamilyAccessService familyAccessService;
    private final UserLookupApi userLookupApi;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(readOnly = true)
    public PollListResp listActive(Long familyId, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return pollListFalse("family not found or access denied");
        }

        LocalDateTime now = LocalDateTime.now();
        List<PollResp> items = pollRepository
                .findAllByFamilyIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(familyId, PollStatus.ACTIVE, now)
                .stream()
                .map(this::toResp)
                .toList();
        return pollListTrue(items);
    }

    @Transactional(readOnly = true)
    public PollListResp listClosed(Long familyId, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return pollListFalse("family not found or access denied");
        }

        LocalDateTime now = LocalDateTime.now();
        List<PollResp> items = pollRepository
                .findAllByFamilyIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(familyId, PollStatus.CLOSED, now)
                .stream()
                .map(this::toResp)
                .toList();
        return pollListTrue(items);
    }

    @Transactional(readOnly = true)
    public PollResultResp findActive(Long familyId, Long pollId, Authentication authentication) {
        return find(familyId, pollId, PollStatus.ACTIVE, authentication);
    }

    @Transactional(readOnly = true)
    public PollResultResp findClosed(Long familyId, Long pollId, Authentication authentication) {
        return find(familyId, pollId, PollStatus.CLOSED, authentication);
    }

    @Transactional
    public PollCreateResultResp create(Long familyId, PollCreateReq req, Authentication authentication) {
        String question = normalize(req == null ? null : req.question());
        if (question == null) {
            return pollCreateFalse("question is required");
        }
        if (question.length() > MAX_QUESTION_LENGTH) {
            return pollCreateFalse("question is too long");
        }

        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return pollCreateFalse("family not found or access denied");
        }

        MemberEntity creator = memberCrudService.findByFamilyIdAndUserId(familyId, user.id());
        LocalDateTime now = LocalDateTime.now();

        PollEntity poll = new PollEntity();
        poll.setFamilyId(familyId);
        poll.setCreatorMemberId(creator.getId());
        poll.setQuestion(question);
        poll.setStatus(PollStatus.ACTIVE);
        poll.setCreatedAt(now);
        poll.setExpiresAt(now.plusDays(POLL_TTL_DAYS));

        PollEntity saved = pollRepository.save(poll);
        domainEventPublisher.publish(new PollCreatedEvent(
                saved.getId(),
                familyId,
                creator.getId(),
                user.id(),
                question,
                displayName(creator, user),
                now
        ));

        return new PollCreateResultResp(
                true,
                "",
                new PollCreateResp(saved.getId()),
                toResp(saved),
                LocalDateTime.now()
        );
    }

    @Transactional
    public PollAnswerResultResp addAnswer(Long familyId,
                                          Long pollId,
                                          PollAnswerCreateReq req,
                                          Authentication authentication) {
        String text = normalize(req == null ? null : req.text());
        if (text == null) {
            return answerFalse("answer text is required");
        }
        if (text.length() > MAX_ANSWER_LENGTH) {
            return answerFalse("answer text is too long");
        }

        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return answerFalse("family not found or access denied");
        }

        PollEntity poll = pollRepository.findByIdAndFamilyId(pollId, familyId).orElse(null);
        PollResultResp availabilityError = availabilityError(poll, PollStatus.ACTIVE);
        if (availabilityError != null) {
            return answerFalse(availabilityError.reason());
        }

        MemberEntity member = memberCrudService.findByFamilyIdAndUserId(familyId, user.id());
        PollAnswerEntity answer = new PollAnswerEntity();
        answer.setPoll(poll);
        answer.setMemberId(member.getId());
        answer.setText(text);
        answer.setCreatedAt(LocalDateTime.now());

        PollAnswerEntity saved = answerRepository.save(answer);
        return new PollAnswerResultResp(true, "", toAnswerResp(saved), LocalDateTime.now());
    }

    @Transactional
    public PollResultResp close(Long familyId, Long pollId, Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return pollFalse("family not found or access denied");
        }

        PollEntity poll = pollRepository.findByIdAndFamilyId(pollId, familyId).orElse(null);
        PollResultResp availabilityError = availabilityError(poll, null);
        if (availabilityError != null) {
            return availabilityError;
        }

        if (poll.getStatus() == PollStatus.ACTIVE) {
            poll.setStatus(PollStatus.CLOSED);
            poll.setClosedAt(LocalDateTime.now());
            pollRepository.save(poll);
        }

        return pollTrue(toResp(poll));
    }

    @Scheduled(fixedDelayString = "${family-hub.polls.cleanup-delay-ms:3600000}")
    @Transactional
    public void deleteExpiredPolls() {
        long deleted = pollRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Deleted {} expired family polls", deleted);
        }
    }

    private PollResultResp find(Long familyId,
                                Long pollId,
                                PollStatus expectedStatus,
                                Authentication authentication) {
        UserView user = currentUser(authentication);
        if (!hasAccess(user.id(), familyId)) {
            return pollFalse("family not found or access denied");
        }

        PollEntity poll = pollRepository.findByIdAndFamilyId(pollId, familyId).orElse(null);
        PollResultResp availabilityError = availabilityError(poll, expectedStatus);
        if (availabilityError != null) {
            return availabilityError;
        }
        return pollTrue(toResp(poll));
    }

    private PollResultResp availabilityError(PollEntity poll, PollStatus expectedStatus) {
        if (poll == null) {
            return pollFalse("poll not found");
        }
        if (!poll.getExpiresAt().isAfter(LocalDateTime.now())) {
            return pollFalse("poll expired");
        }
        if (expectedStatus == PollStatus.ACTIVE && poll.getStatus() == PollStatus.CLOSED) {
            return pollFalse("poll is closed");
        }
        if (expectedStatus == PollStatus.CLOSED && poll.getStatus() != PollStatus.CLOSED) {
            return pollFalse("poll is not closed");
        }
        return null;
    }

    private PollResp toResp(PollEntity poll) {
        Map<Long, String> memberNames = memberNames(poll);
        return new PollResp(
                poll.getId(),
                poll.getFamilyId(),
                poll.getQuestion(),
                poll.getCreatorMemberId(),
                memberNames.get(poll.getCreatorMemberId()),
                poll.getStatus(),
                poll.getCreatedAt(),
                poll.getExpiresAt(),
                poll.getClosedAt(),
                poll.getAnswers().stream()
                        .map(answer -> toAnswerResp(answer, memberNames))
                        .toList()
        );
    }

    private PollAnswerResp toAnswerResp(PollAnswerEntity answer) {
        Map<Long, String> memberNames = memberNames(answer.getMemberId());
        return toAnswerResp(answer, memberNames);
    }

    private PollAnswerResp toAnswerResp(PollAnswerEntity answer, Map<Long, String> memberNames) {
        return new PollAnswerResp(
                answer.getId(),
                answer.getPoll().getId(),
                answer.getMemberId(),
                memberNames.get(answer.getMemberId()),
                answer.getText(),
                answer.getCreatedAt()
        );
    }

    private Map<Long, String> memberNames(PollEntity poll) {
        Set<Long> memberIds = new LinkedHashSet<>();
        memberIds.add(poll.getCreatorMemberId());
        for (PollAnswerEntity answer : poll.getAnswers()) {
            memberIds.add(answer.getMemberId());
        }
        return memberNames(memberIds);
    }

    private Map<Long, String> memberNames(Long memberId) {
        Set<Long> memberIds = new LinkedHashSet<>();
        memberIds.add(memberId);
        return memberNames(memberIds);
    }

    private Map<Long, String> memberNames(Set<Long> memberIds) {
        Map<Long, String> names = new HashMap<>();
        for (MemberEntity member : memberRepository.findAllById(memberIds)) {
            names.put(member.getId(), displayName(member));
        }
        return names;
    }

    private String displayName(MemberEntity member) {
        if (member.getDefaultName() != null && !member.getDefaultName().isBlank()) {
            return member.getDefaultName();
        }
        if (member.getUserId() != null) {
            try {
                UserView user = userLookupApi.findUserById(member.getUserId());
                if (user != null && user.username() != null && !user.username().isBlank()) {
                    return user.username();
                }
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    private String displayName(MemberEntity member, UserView user) {
        if (member.getDefaultName() != null && !member.getDefaultName().isBlank()) {
            return member.getDefaultName();
        }
        if (user != null && user.username() != null && !user.username().isBlank()) {
            return user.username();
        }
        return null;
    }

    private UserView currentUser(Authentication authentication) {
        return familyAccessService.currentUser(authentication.getName());
    }

    private boolean hasAccess(Long userId, Long familyId) {
        return familyAccessService.hasFamilyAccess(userId, familyId);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private PollListResp pollListTrue(List<PollResp> items) {
        return new PollListResp(true, "", items, LocalDateTime.now());
    }

    private PollListResp pollListFalse(String reason) {
        return new PollListResp(false, reason, List.of(), LocalDateTime.now());
    }

    private PollResultResp pollTrue(PollResp poll) {
        return new PollResultResp(true, "", poll, LocalDateTime.now());
    }

    private PollResultResp pollFalse(String reason) {
        return new PollResultResp(false, reason, null, LocalDateTime.now());
    }

    private PollCreateResultResp pollCreateFalse(String reason) {
        return new PollCreateResultResp(false, reason, null, null, LocalDateTime.now());
    }

    private PollAnswerResultResp answerFalse(String reason) {
        return new PollAnswerResultResp(false, reason, null, LocalDateTime.now());
    }
}
