package com.memoize.api.Service;

import com.memoize.api.Config.Common;
import com.memoize.api.Dto.AuthenticationResponse;
import com.memoize.api.Dto.RotateRefreshTokenResponse;
import com.memoize.api.Entity.RefreshToken;
import com.memoize.api.Entity.User;
import com.memoize.api.Repository.RefreshTokenRepository;
import com.memoize.api.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final Duration refreshTokenExpiration = Duration.ofMinutes(10);

    @Override
    @Transactional
    public String generateRefreshToken(UUID userId) {
        String secret = Common.generateRandomString(64);
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .tokenHash(Common.PASSWORD_ENCODER.encode(secret))
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
        return refreshTokenEntity.getTokenId().toString() + "." + secret;
    }

    @Override
    public boolean validateRefreshToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) return false;
        UUID tokenId;
        try {
            tokenId = UUID.fromString(parts[0]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        String rawSecret = parts[1];

        return refreshTokenRepository.findById(tokenId)
                .filter(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(rt -> Common.PASSWORD_ENCODER.matches(rawSecret, rt.getTokenHash()))
                .map(rt -> userRepository.existsById(rt.getUserId()))
                .orElse(false);
    }

    @Override
    @Transactional
    public void removeRefreshToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) return;
        UUID tokenId;
        try {
            tokenId = UUID.fromString(parts[0]);
        } catch (IllegalArgumentException e) {
            return;
        }
        refreshTokenRepository.deleteById(tokenId);
    }

    @Override
    public String rotateRefreshToken(String oldToken) {
        String[] parts = oldToken.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid refresh token format");
        }
        UUID oldTokenId = UUID.fromString(parts[0]);
        RefreshToken oldRefreshToken = refreshTokenRepository.findById(oldTokenId)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        UUID userId = oldRefreshToken.getUserId();
        // Invalidate old token
        refreshTokenRepository.deleteById(oldTokenId);
        // Generate new token
        return generateRefreshToken(userId);
    }

    @Override
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true).secure(true)
                .path("/auth/refresh").sameSite("Lax")
                .maxAge(this.refreshTokenExpiration)
                .build();
    }

    @Override
    public RotateRefreshTokenResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || !validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String[] parts = refreshToken.split("\\.");
        UUID tokenId = UUID.fromString(parts[0]);
        RefreshToken oldRefreshTokenEntity = refreshTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        UUID userId = oldRefreshTokenEntity.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String newJwtToken = jwtService.generateToken(userId.toString(), user.getRole().name());
        refreshTokenRepository.delete(oldRefreshTokenEntity);
        String newRefreshToken = generateRefreshToken(userId);
        return RotateRefreshTokenResponse.builder()
                .refreshToken(newRefreshToken)
                .authResponse(AuthenticationResponse.of(newJwtToken, userId))
                .build();
    }
}
