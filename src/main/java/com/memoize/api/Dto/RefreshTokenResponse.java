package com.memoize.api.Dto;

import lombok.Builder;

@Builder
public record RefreshTokenResponse(String refreshToken, String accessToken) {
}
