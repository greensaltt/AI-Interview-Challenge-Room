package com.offerdungeon.auth.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username must not be blank.")
                @Size(min = 4, max = 32, message = "username length must be between 4 and 32 characters.")
                @Pattern(
                        regexp = "^[A-Za-z0-9_]+$",
                        message = "username may only contain letters, numbers, and underscores.")
                String username,
        @NotBlank(message = "email must not be blank.")
                @Email(message = "email must be a valid email address.")
                @Size(max = 128, message = "email length must not exceed 128 characters.")
                String email,
        @NotBlank(message = "password must not be blank.")
                @Size(min = 6, max = 64, message = "password length must be between 6 and 64 characters.")
                String password,
        @Size(max = 64, message = "nickname length must not exceed 64 characters.")
                String nickname) {}
