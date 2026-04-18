package com.offerdungeon.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "account must not be blank.")
                @Size(max = 128, message = "account length must not exceed 128 characters.")
                String account,
        @NotBlank(message = "password must not be blank.")
                @Size(min = 6, max = 64, message = "password length must be between 6 and 64 characters.")
                String password) {}
