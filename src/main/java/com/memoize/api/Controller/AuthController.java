package com.memoize.api.Controller;

import com.memoize.api.Dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Null;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthController {
    ResponseEntity<CommonResponse<AuthenticationResponse>> userLogin(LoginRequest loginRequest);

    ResponseEntity<CommonResponse<AuthenticationResponse>> userSignup(SignupRequest signupRequest);

    ResponseEntity<CommonResponse<AuthenticationResponse>> refreshToken(HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<CommonResponse<Null>> logout(HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<CommonResponse<Boolean>> isUsernameAvailable(String username);

    ResponseEntity<CommonResponse<Boolean>> isEmailAvailable(String email);

    ResponseEntity<CommonResponse<Boolean>> sendNewVerificationEmail(EmailRequest emailRequest);


}
