package com.resale.loveresalecustomer.components.auth.dto.register;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequestDTO {
    private String fullName;
    private String email;
    private String countryCode;
    private String mobile;
    private String nationality;
    private String address;
    private String nationalId;
    private String password;
    private String repeatPassword;
    private String passportNumber;
}
