package com.memoize.api.Service;

import com.memoize.api.Dto.RefreshTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.util.UUID;

public interface RefreshTokenService {
    String generateRefreshToken(UUID userId);
    RefreshTokenResponse refreshAccessToken(HttpServletRequest request);
    void logoutHandler(HttpServletRequest request);
    ResponseCookie createRefreshTokenCookie(String refreshToken);
    ResponseCookie deleteRefreshTokenCookie();
}
