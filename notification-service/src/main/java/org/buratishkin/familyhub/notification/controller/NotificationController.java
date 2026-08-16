package org.buratishkin.familyhub.notification.controller;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.notification.dto.NotificationListResp;
import org.buratishkin.familyhub.notification.dto.NotificationResp;
import org.buratishkin.familyhub.notification.service.CurrentUserService;
import org.buratishkin.familyhub.notification.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    @GetMapping
    public NotificationListResp list(@RequestParam(defaultValue = "false") boolean unreadOnly,
                                     @RequestParam(required = false) Integer limit,
                                     Authentication authentication) {
        Long userId = currentUserService.requireCurrentUserId(authentication);
        return notificationService.list(userId, unreadOnly, limit);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResp markRead(@PathVariable Long notificationId, Authentication authentication) {
        Long userId = currentUserService.requireCurrentUserId(authentication);
        return notificationService.markRead(userId, notificationId);
    }

    @PostMapping("/read-all")
    public void markAllRead(Authentication authentication) {
        Long userId = currentUserService.requireCurrentUserId(authentication);
        notificationService.markAllRead(userId);
    }
}
