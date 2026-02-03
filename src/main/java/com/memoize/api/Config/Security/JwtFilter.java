package com.memoize.api.Config.Security;

import com.memoize.api.Config.Common;
import com.memoize.api.Dto.ErrorResponse;
import com.memoize.api.Service.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtServiceImpl jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws IOException {
        try {
            // bypass for public endpoints
            AntPathMatcher matcher = new AntPathMatcher();
            String requestUri = request.getRequestURI();
            for (String endpoint : Common.PUBLIC_ENDPOINTS) {
                if (matcher.match(endpoint, requestUri)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            // check authentication header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ServletException("Authorization header missing");
            }
            // extract token
            String token = authHeader.substring("Bearer ".length());
            if (!jwtService.isTokenValid(token)) {
                throw new JwtException("Invalid or Expired token");
            }
            // extract claims
            Claims claims = jwtService.extractClaims(token);
            UUID userId = UUID.fromString(claims.getSubject());
            String role = claims.get("role", String.class);
            // set authentication
            AuthPrincipal principal = new AuthPrincipal(userId, role);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            // continue in filter chain
            filterChain.doFilter(request, response);
        } catch (ServletException | JwtException | IllegalArgumentException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(e.getLocalizedMessage())));
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(e.getLocalizedMessage())));
        }
    }
}
