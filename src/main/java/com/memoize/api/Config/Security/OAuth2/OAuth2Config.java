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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class OAuth2Config {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${spring.app.client-url}")
    private String clientUrl;

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            System.out.println("OAuth2 Error: " + exception.getMessage());
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
            String provider = oAuthToken.getAuthorizedClientRegistrationId();
            OAuth2UserInfo userInfo = getOAuth2UserInfo(oAuthUser, provider);
            AuthenticationResponse authResponse = null;
            try {
                authResponse = oAuth2Login(userInfo, provider);
                String refreshToken = refreshTokenService.generateRefreshToken(authResponse.userId());
                ResponseCookie refreshTokenCookie = refreshTokenService.createRefreshTokenCookie(refreshToken);
                response.setHeader("Set-Cookie", refreshTokenCookie.toString());
                response.sendRedirect(clientUrl + "/oauth2redirect");
            } catch (Exception ex) {
                response.setStatus(401);
                String rawError = ex.getMessage() != null ? ex.getMessage() : "OAuth authentication failed";
                String encodedError = URLEncoder.encode(rawError, StandardCharsets.UTF_8);
                response.sendRedirect(clientUrl + "/oauth2redirect?error=" + encodedError);
            }
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


    protected AuthenticationResponse oAuth2Login(OAuth2UserInfo userInfo, String provider) {
        User existingUser = userRepository.findByIdentifier(userInfo.email()).orElse(null);
        AuthenticationResponse authResponse = null;
        if (existingUser != null) {
            if (!provider.equalsIgnoreCase(existingUser.getAuthSource().toString())) {
                throw new IllegalArgumentException("This email is registered in Memoize with a different AuthSource");
            }
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
                    .authSource(AuthSource.fromString(provider))
                    .build();
            userRepository.saveAndFlush(newUser);
            String jwtToken = jwtService.generateToken(newUser.getId().toString(), newUser.getRole().name());
            authResponse = AuthenticationResponse.of(jwtToken, newUser.getId());
        }
        return authResponse;
    }
}
