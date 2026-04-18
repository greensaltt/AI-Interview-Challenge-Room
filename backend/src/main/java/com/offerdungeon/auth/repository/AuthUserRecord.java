package com.offerdungeon.auth.repository;

public record AuthUserRecord(
        Long id,
        String username,
        String email,
        String passwordHash,
        String nickname,
        String userStatus) {}
