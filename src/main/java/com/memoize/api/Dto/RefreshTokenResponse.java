package com.memoize.api.Dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RefreshTokenResponse {
    private String refreshToken;
    private String accessToken;
}
