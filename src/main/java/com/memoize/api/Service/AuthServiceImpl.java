package com.memoize.api.Service;

import com.memoize.api.Config.Common;
import com.memoize.api.Dto.AuthenticationResponse;
import com.memoize.api.Dto.LoginRequest;
import com.memoize.api.Dto.SignupRequest;
import com.memoize.api.Entity.User;
import com.memoize.api.Enum.Role;
import com.memoize.api.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        String identifier = request.getIdentifier();
        String password = request.getPassword();
        Authentication auth = authenticationManager.
                authenticate(new UsernamePasswordAuthenticationToken(identifier, password));
        User authenticatedUser = (User) auth.getPrincipal();
        assert authenticatedUser != null;
        String jwtToken = jwtService.generateToken(authenticatedUser.getId().toString(), authenticatedUser.getRole().name());
        return AuthenticationResponse.of(jwtToken, authenticatedUser.getId());
    }

    @Override
    @Transactional
    public AuthenticationResponse signup(SignupRequest request) {
        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(Common.PASSWORD_ENCODER.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user.getId().toString(), user.getRole().name());
        return AuthenticationResponse.of(jwtToken, user.getId());
    }

    @Override
    public Boolean isUsernameAvailable(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }

    @Override
    public Boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    @Override
    public void logout() {

    }

}
