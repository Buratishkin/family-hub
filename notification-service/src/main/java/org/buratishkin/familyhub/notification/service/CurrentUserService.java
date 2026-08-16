package org.buratishkin.familyhub.notification.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.notification.projection.NotificationProjectionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final NotificationProjectionService projectionService;

    public Long requireCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return projectionService.findUserIdByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Current user is not projected in notification-service yet"
                ));
    }
}
