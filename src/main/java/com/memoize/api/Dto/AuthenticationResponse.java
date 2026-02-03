package com.memoize.api.Dto;

import lombok.*;

import net.minidev.json.annotate.JsonIgnore;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthenticationResponse {
    @NonNull
    private String accessToken;
    private UUID userId;

    @JsonIgnore
    public UUID getUserId() {
        return userId;
    }

    public static AuthenticationResponse of(String token, UUID userId) {
        return new AuthenticationResponse(token, userId);
    }
    public static AuthenticationResponse of(String token) {
        return new AuthenticationResponse(token, null);
    }
}
