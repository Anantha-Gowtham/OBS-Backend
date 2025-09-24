package com.obs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(
        @Email String email,
        @NotBlank String otp
) {}
