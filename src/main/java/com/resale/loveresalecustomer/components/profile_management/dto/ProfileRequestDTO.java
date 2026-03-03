package com.resale.loveresalecustomer.components.profile_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequestDTO {
    private String fullName;
    private String phoneNumber;
    private String oldPassword;
    private String newPassword;
    private String nationality;
    private String recaptcha;
    private String mail;
}
