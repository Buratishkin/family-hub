package org.buratishkin.familyhub.auth.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.buratishkin.familyhub.auth.user.UserEntity;
import org.buratishkin.familyhub.auth.security.TokenType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${family-hub.security.jwt.secret}")
    private String secret;

    @Value("${family-hub.security.jwt.access-expiration:900000}")
    private long accessTokenExpiration;

    @Value("${family-hub.security.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiration;

    public String generateAccessToken(UserEntity user) {
        return buildToken(
                user,
                accessTokenExpiration,
                TokenType.ACCESS
        );
    }

    public String generateRefreshToken(UserEntity user) {
        return buildToken(
                user,
                refreshTokenExpiration,
                TokenType.REFRESH
        );
    }

    private String buildToken(UserEntity user, long expirationMs, TokenType tokenType) {
        JwtBuilder builder = Jwts.builder()
                .subject(user.getUsername())
                .claim("type", tokenType.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignInKey());

        return builder.compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && isAccessToken(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public void validateRefreshToken(String token) {
        Claims claims = extractAllClaims(token);

        String typeStr = claims.get("type", String.class);
        if (!TokenType.REFRESH.name().equals(typeStr)) {
            throw new RuntimeException("Token is not a refresh token");
        }
    }

    private boolean isAccessToken(String token) {
        Claims claims = extractAllClaims(token);
        String typeStr = claims.get("type", String.class);
        return TokenType.ACCESS.name().equals(typeStr);
    }
}
