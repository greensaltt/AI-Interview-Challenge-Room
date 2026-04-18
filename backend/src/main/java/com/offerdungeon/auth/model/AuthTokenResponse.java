package com.offerdungeon.auth.model;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        String expiresAt,
        CurrentUserResponse user) {}
