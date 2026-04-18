package com.offerdungeon.auth.model;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record AuthenticatedUser(
        Long id,
        String username,
        String email,
        String nickname,
        String userStatus,
        List<String> roleCodes) {

    public AuthenticatedUser {
        roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
    }

    public Collection<? extends GrantedAuthority> authorities() {
        return roleCodes.stream().map(SimpleGrantedAuthority::new).toList();
    }
}
