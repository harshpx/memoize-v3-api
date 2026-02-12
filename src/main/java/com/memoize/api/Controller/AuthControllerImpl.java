package com.memoize.api.Controller;

import com.memoize.api.Dto.*;
import com.memoize.api.Service.AuthService;
import com.memoize.api.Service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
        var refreshToken = refreshTokenService.generateRefreshToken(authResponse.userId());
        var refreshTokenCookie = refreshTokenService.createRefreshTokenCookie(refreshToken);
        return ResponseEntity.ok()
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(CommonResponse.success(authResponse));
    }

    @Override
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<AuthenticationResponse>> userSignup(@Valid @RequestBody SignupRequest signupRequest) {
        var authResponse = authService.signup(signupRequest);
        var refreshToken = refreshTokenService.generateRefreshToken(authResponse.userId());
        var refreshTokenCookie = refreshTokenService.createRefreshTokenCookie(refreshToken);
        return ResponseEntity.ok()
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(CommonResponse.success(authResponse));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<AuthenticationResponse>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        RefreshTokenResponse tokenData = refreshTokenService.refreshAccessToken(request);
        String newRefreshToken = tokenData.refreshToken();
        String newAccessToken = tokenData.accessToken();

        var authResponse = AuthenticationResponse.of(newAccessToken);
        var refreshTokenCookie = refreshTokenService.createRefreshTokenCookie(newRefreshToken);

        return ResponseEntity.ok()
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(CommonResponse.success(authResponse));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Null>> logout(HttpServletRequest request, HttpServletResponse response) {
        refreshTokenService.logoutHandler(request);
        ResponseCookie deleteCookie = refreshTokenService.deleteRefreshTokenCookie();
        return ResponseEntity.ok().header("Set-Cookie", deleteCookie.toString()).body(CommonResponse.success(null));
    }

    @Override
    @GetMapping("/check-username")
    public ResponseEntity<CommonResponse<Boolean>> isUsernameAvailable(@RequestParam(name = "username") String username) {
        return ResponseEntity.ok(CommonResponse.success(authService.isUsernameAvailable(username)));
    }

    @Override
    @GetMapping("/check-email")
    public ResponseEntity<CommonResponse<Boolean>> isEmailAvailable(@RequestParam(name = "email") String email) {
        return ResponseEntity.ok(CommonResponse.success(authService.isEmailAvailable(email)));
    }

    @Override
    @PostMapping("/email-verification")
    public ResponseEntity<CommonResponse<Boolean>> sendNewVerificationEmail(@RequestBody Map<String, String> emailData) {
        String email = emailData.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        authService.sendVerificationEmail(email, true);
        return ResponseEntity.ok(CommonResponse.success(true));
    }
}
