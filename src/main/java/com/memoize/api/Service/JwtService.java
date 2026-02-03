package com.memoize.api.Service;

import io.jsonwebtoken.Claims;

public interface JwtService {
    String generateToken(String userId, String role);
    Claims extractClaims(String token);
    boolean isTokenValid(String token);
}
