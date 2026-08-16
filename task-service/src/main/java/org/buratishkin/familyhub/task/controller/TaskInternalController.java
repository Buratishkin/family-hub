package org.buratishkin.familyhub.task.controller;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.task.api.TaskView;
import org.buratishkin.familyhub.task.service.TaskCrudService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/tasks")
public class TaskInternalController {
    private final TaskCrudService taskCrudService;

    @GetMapping("/families/{familyId}")
    public List<TaskView> findTasksByFamilyId(@PathVariable Long familyId) {
        return taskCrudService.findTasksByFamilyId(familyId);
    }

    @GetMapping("/families/{familyId}/old")
    public List<TaskView> findOldTasksByFamilyId(@PathVariable Long familyId) {
        return taskCrudService.findOldTasksByFamilyId(familyId);
    }

    @GetMapping("/members/{memberId}/assignee-count")
    public int countByAssigneeMemberId(@PathVariable Long memberId) {
        return taskCrudService.countByAssigneeMemberId(memberId);
    }

    @GetMapping("/members/{memberId}/creator-count")
    public int countByCreatorMemberId(@PathVariable Long memberId) {
        return taskCrudService.countByCreatorMemberId(memberId);
    }
}
