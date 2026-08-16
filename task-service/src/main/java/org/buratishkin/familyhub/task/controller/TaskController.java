package org.buratishkin.familyhub.task.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.task.dto.TaskCreateReq;
import org.buratishkin.familyhub.task.dto.TaskCreateResultResp;
import org.buratishkin.familyhub.task.dto.TaskDeleteReq;
import org.buratishkin.familyhub.task.dto.TaskUpdateResultResp;
import org.buratishkin.familyhub.task.dto.TaskUpdateReq;
import org.buratishkin.familyhub.task.service.TaskManagerService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/task")
public class TaskController {
    private final TaskManagerService taskManagerService;

    @PostMapping("/")
    public TaskCreateResultResp create(@Valid @RequestBody TaskCreateReq createReq,
                                       Authentication authentication) {
        return taskManagerService.create(createReq, authentication);
    }

    @PutMapping("/{taskId}")
    public TaskUpdateResultResp update(@Valid @RequestBody TaskUpdateReq updateReq,
                                       @PathVariable Long taskId,
                                       Authentication authentication) {
        return taskManagerService.update(updateReq, taskId, authentication);
    }

    @DeleteMapping("/{taskId}")
    public DeleteResp delete(@Valid @RequestBody TaskDeleteReq deleteReq,
                             @PathVariable Long taskId,
                             Authentication authentication) {
        return taskManagerService.delete(deleteReq, taskId, authentication);
    }
}
