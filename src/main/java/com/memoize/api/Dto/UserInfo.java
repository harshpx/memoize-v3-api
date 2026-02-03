package com.memoize.api.Dto;

import com.memoize.api.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserInfo {
    private String id;
    private String name;
    private String username;
    private String email;
    private String avatarUrl;
    private String role;

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
