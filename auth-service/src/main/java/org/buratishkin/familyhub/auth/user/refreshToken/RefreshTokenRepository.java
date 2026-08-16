package org.buratishkin.familyhub.auth.user.refreshToken;

import org.buratishkin.familyhub.auth.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);

    List<RefreshTokenEntity> findAllByUserAndRevokedFalse(UserEntity user);
}
