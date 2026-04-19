package com.offerdungeon.admin.controller;

import com.offerdungeon.auth.model.AccessScopeResponse;
import com.offerdungeon.auth.model.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAccessController {

    @GetMapping("/access-scope")
    public AccessScopeResponse accessScope(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return AccessScopeResponse.from("ADMIN", authenticatedUser);
    }
}
