package com.memoize.api.Controller;

import com.memoize.api.Dto.*;
import com.memoize.api.Service.AuthService;
import com.memoize.api.Service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<AuthenticationResponse>> userLogin(@Valid @RequestBody LoginRequest loginRequest) {
        var authResponse = authService.login(loginRequest);
        var refreshToken = refreshTokenService.generateRefreshToken(authResponse.getUserId());
        var refreshTokenCookie = refreshTokenService.createRefreshTokenCookie(refreshToken);
        return ResponseEntity.ok()
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(CommonResponse.success(authResponse));
    }

    @Override
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<AuthenticationResponse>> userSignup(@Valid @RequestBody SignupRequest signupRequest) {
        var authResponse = authService.signup(signupRequest);
        var refreshToken = refreshTokenService.generateRefreshToken(authResponse.getUserId());
        var refreshTokenCookie = refreshTokenService.createRefreshTokenCookie(refreshToken);
        return ResponseEntity.ok()
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(CommonResponse.success(authResponse));
    }

    @Override
    @GetMapping("/refresh")
    public ResponseEntity<CommonResponse<AuthenticationResponse>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        if (cookies != null) {
            for (var cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        RotateRefreshTokenResponse tokenData = refreshTokenService.refreshAccessToken(refreshToken);
        String newRefreshToken = tokenData.getRefreshToken();
        AuthenticationResponse authResponse = tokenData.getAuthResponse();
        var refreshTokenCookie = refreshTokenService.createRefreshTokenCookie(newRefreshToken);
        return ResponseEntity.ok()
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(CommonResponse.success(authResponse));
    }
}
