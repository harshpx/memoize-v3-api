package com.memoize.api.Service;

import com.memoize.api.Config.Common;
import com.memoize.api.Dto.AuthenticationResponse;
import com.memoize.api.Dto.LoginRequest;
import com.memoize.api.Dto.SignupRequest;
import com.memoize.api.Entity.User;
import com.memoize.api.Entity.VerificationToken;
import com.memoize.api.Enum.Role;
import com.memoize.api.Repository.UserRepository;
import com.memoize.api.Repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        String identifier = request.identifier();
        String password = request.password();
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
        if (!verifyCode(request.email(), request.verificationCode())) {
            throw new IllegalArgumentException("Invalid verification code");
        }
        User user = User.builder()
                .name(request.name())
                .username(request.username())
                .email(request.email())
                .password(Common.PASSWORD_ENCODER.encode(request.password()))
                .role(Role.USER)
                .build();
        userRepository.saveAndFlush(user);
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
    @Transactional
    public void sendVerificationEmail(String email, boolean newRegistration) {
        String verificationCode = Common.generateRandomString(6);
        if (newRegistration && !isEmailAvailable(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (!newRegistration && isEmailAvailable(email)) {
            throw new IllegalArgumentException("Email not found");
        }
        if (verificationTokenRepository.existsValidTokenByEmail(email)) {
            throw new IllegalStateException("A valid verification token already exists for this email, try after 10 minutes");
        }
        var verificationToken = VerificationToken.builder()
                .token(Common.encodeBase64(verificationCode)).email(email).build();
        verificationTokenRepository.saveAndFlush(verificationToken);
        mailService.sendVerificationEmail(email, verificationCode);
    }

    // helper
    private boolean verifyCode(String email, String code) {
        String encodedCode = Common.encodeBase64(code);
        var token = verificationTokenRepository.findByEmailAndToken(email, encodedCode);
        if (token.isEmpty()) {
            return false;
        }
        if (token.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(token.get());
            return false;
        }
        verificationTokenRepository.delete(token.get());
        return true;
    }
}
