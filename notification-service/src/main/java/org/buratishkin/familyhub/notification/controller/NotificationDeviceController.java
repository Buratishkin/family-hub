package org.buratishkin.familyhub.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.notification.dto.DeviceResp;
import org.buratishkin.familyhub.notification.dto.RegisterDeviceReq;
import org.buratishkin.familyhub.notification.service.CurrentUserService;
import org.buratishkin.familyhub.notification.service.NotificationDeviceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notification-devices")
@RequiredArgsConstructor
public class NotificationDeviceController {
    private final CurrentUserService currentUserService;
    private final NotificationDeviceService deviceService;

    @PostMapping
    public DeviceResp register(@Valid @RequestBody RegisterDeviceReq req, Authentication authentication) {
        Long userId = currentUserService.requireCurrentUserId(authentication);
        return deviceService.register(userId, req);
    }

    @GetMapping
    public List<DeviceResp> list(Authentication authentication) {
        Long userId = currentUserService.requireCurrentUserId(authentication);
        return deviceService.list(userId);
    }

    @DeleteMapping("/{deviceId}")
    public void unregister(@PathVariable Long deviceId, Authentication authentication) {
        Long userId = currentUserService.requireCurrentUserId(authentication);
        deviceService.unregister(userId, deviceId);
    }
}
