package com.memoize.api.Service;

import com.memoize.api.Dto.AuthenticationResponse;
import com.memoize.api.Dto.LoginRequest;
import com.memoize.api.Dto.PasswordResetRequest;
import com.memoize.api.Dto.VerificationCodeCheckRequest;
import com.memoize.api.Dto.SignupRequest;
import com.memoize.api.Enum.VerificationType;

public interface AuthService {
    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse signup(SignupRequest request);

    Boolean isUsernameAvailable(String username);

    Boolean isEmailAvailable(String email);

    void verifyPasswordResetCode(VerificationCodeCheckRequest request);

    void resetPassword(PasswordResetRequest request);

    void sendVerificationEmail(String email, VerificationType verificationType);
}
