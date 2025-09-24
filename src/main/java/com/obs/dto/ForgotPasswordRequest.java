package com.obs.dto;

import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
        @Email String email
) {}
