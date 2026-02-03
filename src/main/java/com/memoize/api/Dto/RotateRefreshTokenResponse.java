package com.memoize.api.Dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RotateRefreshTokenResponse {
    private String refreshToken;
    private AuthenticationResponse authResponse;
}
