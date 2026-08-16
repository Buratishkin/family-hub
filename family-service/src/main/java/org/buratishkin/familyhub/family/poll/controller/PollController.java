package org.buratishkin.familyhub.family.poll.controller;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.family.poll.dto.PollAnswerCreateReq;
import org.buratishkin.familyhub.family.poll.dto.PollAnswerResultResp;
import org.buratishkin.familyhub.family.poll.dto.PollCreateReq;
import org.buratishkin.familyhub.family.poll.dto.PollCreateResultResp;
import org.buratishkin.familyhub.family.poll.dto.PollListResp;
import org.buratishkin.familyhub.family.poll.dto.PollResultResp;
import org.buratishkin.familyhub.family.poll.service.PollService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/me/families/{familyId}/polls")
public class PollController {
    private final PollService pollService;

    @GetMapping
    public PollListResp listActive(@PathVariable Long familyId, Authentication authentication) {
        return pollService.listActive(familyId, authentication);
    }

    @GetMapping("/closed")
    public PollListResp listClosed(@PathVariable Long familyId, Authentication authentication) {
        return pollService.listClosed(familyId, authentication);
    }

    @GetMapping("/{pollId}")
    public PollResultResp findActive(@PathVariable Long familyId,
                                     @PathVariable Long pollId,
                                     Authentication authentication) {
        return pollService.findActive(familyId, pollId, authentication);
    }

    @GetMapping("/closed/{pollId}")
    public PollResultResp findClosed(@PathVariable Long familyId,
                                     @PathVariable Long pollId,
                                     Authentication authentication) {
        return pollService.findClosed(familyId, pollId, authentication);
    }

    @PostMapping
    public PollCreateResultResp create(@PathVariable Long familyId,
                                       @RequestBody(required = false) PollCreateReq req,
                                       Authentication authentication) {
        return pollService.create(familyId, req, authentication);
    }

    @PostMapping("/{pollId}/answers")
    public PollAnswerResultResp addAnswer(@PathVariable Long familyId,
                                          @PathVariable Long pollId,
                                          @RequestBody(required = false) PollAnswerCreateReq req,
                                          Authentication authentication) {
        return pollService.addAnswer(familyId, pollId, req, authentication);
    }

    @PostMapping("/{pollId}/close")
    public PollResultResp close(@PathVariable Long familyId,
                                @PathVariable Long pollId,
                                Authentication authentication) {
        return pollService.close(familyId, pollId, authentication);
    }
}
