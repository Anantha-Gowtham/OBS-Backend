package com.obs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank String username,
        @Email String email,
        @NotBlank String password,
        String firstName,
        String lastName,
        String fatherOrSpouseName,
        LocalDate dateOfBirth,
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") 
        String phoneNumber,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits") 
        String pincode,
        @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar number must be 12 digits") 
        String aadhaarNumber,
        @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format") 
        String panNumber,
        String kycDocumentType,
        String kycDocumentNumber,
        @DecimalMin(value = "0.0", message = "Initial deposit cannot be negative") 
        BigDecimal initialDeposit,
        String role
) {}
