package org.buratishkin.familyhub.auth.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.dto.AuthResp;
import org.buratishkin.familyhub.auth.user.dto.LoginReq;
import org.buratishkin.familyhub.auth.user.dto.LogoutResp;
import org.buratishkin.familyhub.auth.user.dto.RefreshReq;
import org.buratishkin.familyhub.auth.user.dto.SignUpReq;
import org.buratishkin.familyhub.auth.user.service.UserManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserManagerService userManagerService;

    @PostMapping("/register")
    public ResponseEntity<AuthResp> register(@RequestBody SignUpReq req) {
        AuthResp response = userManagerService.signUp(req);
        return ResponseEntity.status(response.result() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResp> login(@RequestBody LoginReq req, HttpServletRequest servletRequest) {
        AuthResp response = userManagerService.login(req, servletRequest.getHeader("User-Agent"), extractClientIp(servletRequest));
        return ResponseEntity.status(response.result() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResp> refresh(@RequestBody RefreshReq req, HttpServletRequest servletRequest) {
        AuthResp response = userManagerService.refresh(req.refreshToken(), servletRequest.getHeader("User-Agent"), extractClientIp(servletRequest));
        return ResponseEntity.status(response.result() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResp> logout(@RequestBody RefreshReq req) {
        boolean result = userManagerService.logout(req.refreshToken());
        LogoutResp resp = new LogoutResp(result, result ? "logout successful" : "refresh token not found");
        return ResponseEntity.ok(resp);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int commaIndex = xff.indexOf(',');
            return commaIndex >= 0 ? xff.substring(0, commaIndex).trim() : xff.trim();
        }
        return request.getRemoteAddr();
    }
}
