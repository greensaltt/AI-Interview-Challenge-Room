package com.offerdungeon.auth.model;

import java.util.List;

public record CurrentUserResponse(
        Long id,
        String username,
        String email,
        String nickname,
        String userStatus,
        List<String> roleCodes) {

    public static CurrentUserResponse from(AuthenticatedUser authenticatedUser) {
        return new CurrentUserResponse(
                authenticatedUser.id(),
                authenticatedUser.username(),
                authenticatedUser.email(),
                authenticatedUser.nickname(),
                authenticatedUser.userStatus(),
                authenticatedUser.roleCodes());
    }
}
