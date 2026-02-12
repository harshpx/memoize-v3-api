package com.memoize.api.Dto;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public record AuthenticationResponse(String accessToken, @JsonIgnore UUID userId) {
    public static AuthenticationResponse of(String token, UUID userId) {
        return new AuthenticationResponse(token, userId);
    }
    public static AuthenticationResponse of(String token) {
        return new AuthenticationResponse(token, null);
    }
}
