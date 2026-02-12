package com.memoize.api.Dto;

import com.memoize.api.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
public record UserInfo(String id, String name, String username, String email, String avatarUrl, String role) {
    public static UserInfo fromEntity(User user) {
        return UserInfo.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .build();
    }
}
