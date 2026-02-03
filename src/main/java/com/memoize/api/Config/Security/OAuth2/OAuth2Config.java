package com.memoize.api.Config.Security.OAuth2;

import com.memoize.api.Service.JwtService;
import com.memoize.api.Dto.AuthenticationResponse;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.ErrorResponse;
import com.memoize.api.Entity.User;
import com.memoize.api.Enum.AuthSource;
import com.memoize.api.Enum.Role;
import com.memoize.api.Repository.UserRepository;
import com.memoize.api.Service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
public class OAuth2Config {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of("OAuth2 Authentication Failed")));
        };
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oAuthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuthUser = (OAuth2User) authentication.getPrincipal();
            String provider  = oAuthToken.getAuthorizedClientRegistrationId();
            OAuth2UserInfo userInfo = getOAuth2UserInfo(oAuthUser, provider);
            AuthenticationResponse authResponse = oAuth2Login(userInfo);
            String refreshToken = refreshTokenService.generateRefreshToken(authResponse.getUserId());
            ResponseCookie refreshTokenCookie = refreshTokenService.createRefreshTokenCookie(refreshToken);
            ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(200);
            response.setContentType("application/json");
            response.setHeader("Set-Cookie", refreshTokenCookie.toString());
            response.getWriter().write(objectMapper.writeValueAsString(CommonResponse.success(authResponse)));
        };
    }

    // helper methods
    private OAuth2UserInfo getOAuth2UserInfo(OAuth2User oAuthUser, String provider) {
        if (provider.equalsIgnoreCase("google")) {
            return new OAuth2UserInfoGoogle(oAuthUser);
        }
        // Add other providers here...
        throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
    }

    @Transactional
    private AuthenticationResponse oAuth2Login(OAuth2UserInfo userInfo) {
        User existingUser = userRepository.findByIdentifier(userInfo.email()).orElse(null);
        AuthenticationResponse authResponse = null;
        if (existingUser != null) {
            String jwtToken = jwtService.generateToken(existingUser.getId().toString(), existingUser.getRole().name());
            authResponse = AuthenticationResponse.of(jwtToken, existingUser.getId());
        } else {
            User newUser = User.builder()
                    .username(userInfo.username())
                    .email(userInfo.email())
                    .name(userInfo.name())
                    .avatarUrl(userInfo.avatarUrl())
                    .password("")
                    .role(Role.USER)
                    .authSource(AuthSource.GOOGLE)
                    .build();
            userRepository.save(newUser);
            String jwtToken = jwtService.generateToken(newUser.getId().toString(), newUser.getRole().name());
            authResponse = AuthenticationResponse.of(jwtToken, newUser.getId());
        }
        return authResponse;
    }
}
