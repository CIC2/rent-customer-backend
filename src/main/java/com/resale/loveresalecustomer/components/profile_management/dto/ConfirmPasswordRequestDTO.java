package com.resale.loveresalecustomer.components.profile_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPasswordRequestDTO {
    private String countryCode;
    private String identifier;   // mobile or email
    private String newPassword;
    private String confirmPassword;
}
