package com.memoize.api.Controller;

import com.memoize.api.Dto.AuthenticationResponse;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.LoginRequest;
import com.memoize.api.Dto.SignupRequest;
import com.nimbusds.openid.connect.sdk.LogoutRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Null;
import org.springframework.http.ResponseEntity;

public interface AuthController {
    ResponseEntity<CommonResponse<AuthenticationResponse>> userLogin(LoginRequest loginRequest);
    ResponseEntity<CommonResponse<AuthenticationResponse>> userSignup(SignupRequest signupRequest);
    ResponseEntity<CommonResponse<AuthenticationResponse>> refreshToken(HttpServletRequest request, HttpServletResponse response);
    ResponseEntity<CommonResponse<Null>> logout(HttpServletRequest request, HttpServletResponse response);
    ResponseEntity<CommonResponse<Boolean>> isUsernameAvailable(String username);
    ResponseEntity<CommonResponse<Boolean>> isEmailAvailable(String email);
}
