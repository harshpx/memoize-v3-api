package com.memoize.api.Controller;

import com.memoize.api.Dto.AuthenticationResponse;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.LoginRequest;
import com.memoize.api.Dto.SignupRequest;
import com.nimbusds.openid.connect.sdk.LogoutRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthController {
    ResponseEntity<CommonResponse<AuthenticationResponse>> userLogin(LoginRequest loginRequest);
    ResponseEntity<CommonResponse<AuthenticationResponse>> userSignup(SignupRequest signupRequest);
    ResponseEntity<CommonResponse<AuthenticationResponse>> refreshToken(HttpServletRequest request, HttpServletResponse response);
}
