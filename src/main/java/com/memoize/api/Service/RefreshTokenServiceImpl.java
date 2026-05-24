package com.memoize.api.Service;

import com.memoize.api.Config.Common;
import com.memoize.api.Dto.RefreshTokenResponse;
import com.memoize.api.Entity.RefreshToken;
import com.memoize.api.Entity.User;
import com.memoize.api.Repository.RefreshTokenRepository;
import com.memoize.api.Repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
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

    private final Duration refreshTokenExpiration = Duration.ofDays(10);

    @Override
    @Transactional
    public String generateRefreshToken(UUID userId) {
        String secret = Common.generateRandomString(64);
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .tokenHash(Common.PASSWORD_ENCODER.encode(secret))
                .userId(userId)
                .expiresAt(LocalDateTime.now().plus(refreshTokenExpiration))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
        return refreshTokenEntity.getTokenId().toString() + "." + secret;
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshAccessToken(HttpServletRequest request) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        RefreshToken oldRefreshTokenEntity = validateAndGetRefreshTokenEntity(refreshToken);
        UUID userId = oldRefreshTokenEntity.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new BadCredentialsException("User not found"));
        String newJwtToken = jwtService.generateToken(userId.toString(), user.getRole().name());
        refreshTokenRepository.delete(oldRefreshTokenEntity);
        String newRefreshToken = generateRefreshToken(userId);
        return RefreshTokenResponse.builder()
                .refreshToken(newRefreshToken)
                .accessToken(newJwtToken)
                .build();
    }

    @Override
    @Transactional
    public void logoutHandler(HttpServletRequest request) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        removeRefreshToken(refreshToken);
    }


    @Override
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true).secure(true)
                .path("/auth/refresh").sameSite("None")
                .maxAge(this.refreshTokenExpiration)
                .build();
    }

    @Override
    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true)
                .path("/auth/refresh").sameSite("None")
                .maxAge(0)
                .build();
    }

    // helpers
    public RefreshToken validateAndGetRefreshTokenEntity(String token) throws BadCredentialsException {
        if (token == null || token.isBlank()) throw new BadCredentialsException("No refresh token present");
        String[] parts = token.split("\\.");
        if (parts.length != 2) throw new BadCredentialsException("Invalid refresh token");
        UUID tokenId = UUID.fromString(parts[0]);
        String rawSecret = parts[1];

        return refreshTokenRepository.findById(tokenId)
                .filter(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(rt -> Common.PASSWORD_ENCODER.matches(rawSecret, rt.getTokenHash()))
                .filter(rt -> userRepository.existsById(rt.getUserId())).orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
    }

    @Transactional
    public void removeRefreshToken(String token) {
        if (token == null || token.isBlank()) return;
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
}
