package com.offerdungeon.auth.model;

import java.util.List;

public record AccessScopeResponse(
        String scope,
        Long userId,
        String username,
        String nickname,
        List<String> roleCodes) {

    public static AccessScopeResponse from(String scope, AuthenticatedUser authenticatedUser) {
        return new AccessScopeResponse(
                scope,
                authenticatedUser.id(),
                authenticatedUser.username(),
                authenticatedUser.nickname(),
                authenticatedUser.roleCodes());
    }
}
