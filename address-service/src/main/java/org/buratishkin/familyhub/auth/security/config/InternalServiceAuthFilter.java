package org.buratishkin.familyhub.auth.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InternalServiceAuthFilter extends OncePerRequestFilter {
    public static final String AUTHORITY = "INTERNAL_SERVICE";

    private final InternalServiceProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getServletPath().startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String expectedToken = properties.getToken();
        String actualToken = request.getHeader(properties.getHeaderName());
        if (isValidToken(actualToken, expectedToken)) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "internal-service",
                    null,
                    List.of(new SimpleGrantedAuthority(AUTHORITY))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Invalid internal service token\"}");
    }

    private boolean isValidToken(String actualToken, String expectedToken) {
        if (actualToken == null || actualToken.isBlank() || expectedToken == null || expectedToken.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                actualToken.getBytes(StandardCharsets.UTF_8),
                expectedToken.getBytes(StandardCharsets.UTF_8)
        );
    }
}
