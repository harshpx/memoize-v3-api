package com.memoize.api.Dto;

import com.memoize.api.Entity.User;
import lombok.Builder;

@Builder
public record UserDto(String id, String name, String username, String email, String avatarUrl, String role) {
    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .build();
    }
}
