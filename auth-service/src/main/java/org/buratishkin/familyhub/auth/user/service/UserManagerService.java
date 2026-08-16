package org.buratishkin.familyhub.auth.user.service;

import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.auth.user.refreshToken.RefreshTokenEntity;
import org.buratishkin.familyhub.auth.user.refreshToken.RefreshTokenRepository;
import org.buratishkin.familyhub.auth.user.api.event.UserRegisteredEvent;
import org.buratishkin.familyhub.auth.user.dto.*;
import org.buratishkin.familyhub.auth.user.mapper.UserMapper;
import org.buratishkin.familyhub.auth.user.UserEntity;
import org.buratishkin.familyhub.auth.security.jwt.JwtService;
import org.buratishkin.familyhub.auth.security.service.TokenHashService;
import org.buratishkin.familyhub.shared.event.DomainEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserManagerService {
    private final UserCrudService userCrudService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenHashService tokenHashService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Value("${family-hub.security.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationMs;

    @Transactional
    public AuthResp signUp(SignUpReq createReq){
        if (userCrudService.existsByUsernameOrEmail(createReq.getUsername(), createReq.getEmail()))
            return new AuthResp(
                    false,
                    "invalid credentials",
                    null,
                    null,
                    null,
                    null,
                    null
            );

        String hashedPassword = passwordEncoder.encode(createReq.getPassword());
        UserEntity user = userMapper.toEntity(createReq, hashedPassword);
        user = userCrudService.save(user);
        domainEventPublisher.publish(new UserRegisteredEvent(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                LocalDateTime.now()
        ));

        return new AuthResp(
                true,
                "user created",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null,
                null
        );
    }

    public AuthResp login(LoginReq enterReq){
        return login(enterReq, null, null);
    }

    public AuthResp login(LoginReq enterReq, String userAgent, String ipAddress){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(enterReq.username(), enterReq.password())
            );

            UserEntity user = userCrudService.findByLogin(enterReq.username());
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            RefreshTokenEntity tokenEntity = new RefreshTokenEntity();
            tokenEntity.setUser(user);
            tokenEntity.setRefreshToken(tokenHashService.sha256(refreshToken));
            tokenEntity.setExpiredAt(Instant.now().plusMillis(refreshTokenExpirationMs));
            tokenEntity.setRevoked(false);
            tokenEntity.setUserAgent(userAgent);
            tokenEntity.setIpAddress(ipAddress);
            refreshTokenRepository.save(tokenEntity);

            return new AuthResp(
                    true,
                    "login successful",
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    accessToken,
                    refreshToken
            );
        } catch (Exception e) {
            return new AuthResp(
                    false,
                    "invalid credentials",
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    public AuthResp refresh(String oldRefreshToken, String userAgent, String ipAddress){
        try {
            jwtService.validateRefreshToken(oldRefreshToken);
            RefreshTokenEntity tokenEntity = refreshTokenRepository
                    .findByRefreshToken(tokenHashService.sha256(oldRefreshToken))
                    .orElseThrow(() -> new RuntimeException("refresh token not found"));

            if (tokenEntity.getExpiredAt().isBefore(Instant.now())) {
                throw new RuntimeException("refresh token expired");
            }
            if (tokenEntity.isRevoked()) {
                throw new RuntimeException("refresh token revoked");
            }

            String username = jwtService.extractUsername(oldRefreshToken);
            UserEntity user = userCrudService.findByUsername(username);
            if (!tokenEntity.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("token does not belong to user");
            }

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            tokenEntity.setRefreshToken(tokenHashService.sha256(refreshToken));
            tokenEntity.setExpiredAt(Instant.now().plusMillis(refreshTokenExpirationMs));
            tokenEntity.setUserAgent(userAgent);
            tokenEntity.setIpAddress(ipAddress);
            refreshTokenRepository.save(tokenEntity);

            return new AuthResp(
                    true,
                    "token refreshed",
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    accessToken,
                    refreshToken
            );
        } catch (Exception e) {
            return new AuthResp(
                    false,
                    "invalid refresh token",
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    public boolean logout(String refreshToken){
        return refreshTokenRepository.findByRefreshToken(tokenHashService.sha256(refreshToken))
                .map(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    return true;
                })
                .orElse(false);
    }
}
