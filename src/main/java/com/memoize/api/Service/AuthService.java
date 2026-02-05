package com.memoize.api.Service;

import com.memoize.api.Config.Security.OAuth2.OAuth2UserInfo;
import com.memoize.api.Dto.AuthenticationResponse;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.LoginRequest;
import com.memoize.api.Dto.SignupRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

public interface AuthService {
    AuthenticationResponse login(LoginRequest request);
    AuthenticationResponse signup(SignupRequest request);
    Boolean isUsernameAvailable(String username);
    Boolean isEmailAvailable(String email);
    void logout();

}
