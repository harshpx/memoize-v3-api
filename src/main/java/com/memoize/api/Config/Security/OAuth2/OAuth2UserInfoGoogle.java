package com.memoize.api.Config.Security.OAuth2;

import com.memoize.api.Config.Common;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;

public record OAuth2UserInfoGoogle(OAuth2User user) implements OAuth2UserInfo {
    @Override
    public String email() {
        return user.getAttribute("email");
    }

    @Override
    public String username() {
        String name = user.getAttribute("name");
        if (name == null) return user.getAttribute("sub");
        return name.toLowerCase().replaceAll("\\s+", "_") + "_" + Common.generateRandomString(6);
    }

    @Override
    public String name() {
        return user.getAttribute("name");
    }

    @Override
    public String avatarUrl() {
        return user.getAttribute("picture");
    }
}
