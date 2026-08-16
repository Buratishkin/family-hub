package org.buratishkin.familyhub.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.buratishkin.familyhub.notification.device.DevicePlatform;

public record RegisterDeviceReq(
        @NotBlank String deviceToken,
        @NotNull DevicePlatform platform,
        String displayName
) {
}
