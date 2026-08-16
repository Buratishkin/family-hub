package org.buratishkin.familyhub.task.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.task.dto.TaskRecurrenceCancelReq;
import org.buratishkin.familyhub.task.dto.TaskRecurrenceCancelResp;
import org.buratishkin.familyhub.task.service.TaskRecurrenceSeriesManagerService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/task-recurrence-series")
public class TaskRecurrenceSeriesController {
    private final TaskRecurrenceSeriesManagerService recurrenceSeriesManagerService;

    @PostMapping("/{seriesId}/cancel-remaining")
    public TaskRecurrenceCancelResp cancelRemaining(@PathVariable Long seriesId,
                                                    @Valid @RequestBody TaskRecurrenceCancelReq req,
                                                    Authentication authentication) {
        return recurrenceSeriesManagerService.cancelRemaining(seriesId, req, authentication);
    }
}
