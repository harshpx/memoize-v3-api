package com.memoize.api.Service;

import com.memoize.api.Dto.AuthenticationResponse;
import com.memoize.api.Dto.LoginRequest;
import com.memoize.api.Dto.SignupRequest;

public interface AuthService {
    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse signup(SignupRequest request);

    Boolean isUsernameAvailable(String username);

    Boolean isEmailAvailable(String email);

    void sendVerificationEmail(String email, boolean newRegistration);
}
