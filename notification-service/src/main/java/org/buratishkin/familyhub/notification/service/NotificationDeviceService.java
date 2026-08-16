package org.buratishkin.familyhub.notification.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.notification.device.NotificationDeviceEntity;
import org.buratishkin.familyhub.notification.device.NotificationDeviceRepository;
import org.buratishkin.familyhub.notification.dto.DeviceResp;
import org.buratishkin.familyhub.notification.dto.RegisterDeviceReq;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationDeviceService {
    private final NotificationDeviceRepository deviceRepository;

    @Transactional
    public DeviceResp register(Long userId, RegisterDeviceReq req) {
        LocalDateTime now = LocalDateTime.now();
        NotificationDeviceEntity device = deviceRepository.findByUserIdAndDeviceToken(userId, req.deviceToken())
                .orElseGet(() -> {
                    NotificationDeviceEntity created = new NotificationDeviceEntity();
                    created.setUserId(userId);
                    created.setDeviceToken(req.deviceToken());
                    created.setCreatedAt(now);
                    return created;
                });

        device.setPlatform(req.platform());
        device.setDisplayName(req.displayName());
        device.setActive(true);
        device.setUpdatedAt(now);
        device.setLastSeenAt(now);
        return DeviceResp.from(deviceRepository.save(device));
    }

    public List<DeviceResp> list(Long userId) {
        return deviceRepository.findAllByUserIdAndActiveTrue(userId).stream()
                .map(DeviceResp::from)
                .toList();
    }

    @Transactional
    public void unregister(Long userId, Long deviceId) {
        NotificationDeviceEntity device = deviceRepository.findByIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
        device.setActive(false);
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(device);
    }
}
