package com.offerdungeon.user.controller;

import com.offerdungeon.auth.model.AccessScopeResponse;
import com.offerdungeon.auth.model.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserAccessController {

    @GetMapping("/access-scope")
    public AccessScopeResponse accessScope(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return AccessScopeResponse.from("USER", authenticatedUser);
    }
}
