package com.memoize.api.Service;

import com.memoize.api.Dto.AuthenticationResponse;
import com.memoize.api.Dto.RotateRefreshTokenResponse;
import org.springframework.http.ResponseCookie;

import java.util.UUID;

public interface RefreshTokenService {
    String generateRefreshToken(UUID userId);
    boolean validateRefreshToken(String token);
    void removeRefreshToken(String token);
    String rotateRefreshToken(String oldToken);
    ResponseCookie createRefreshTokenCookie(String refreshToken);
    RotateRefreshTokenResponse refreshAccessToken(String refreshToken);
}
